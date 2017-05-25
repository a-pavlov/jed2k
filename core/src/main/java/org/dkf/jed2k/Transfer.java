package org.dkf.jed2k;

import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.alert.*;
import org.dkf.jed2k.data.PieceBlock;
import org.dkf.jed2k.exception.BaseErrorCode;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.BitField;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.Hash;
import org.dkf.jed2k.protocol.TransferResumeData;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
public class Transfer {
    public static long INVALID_ETA = -1;

    /**
     * transfer's file hash
     */
    private Hash hash;

    private long createTime;

    /**
     * transfer's file dataSize
     */
    private long size;

    /**
     * num pieces in file
     */
    private int numPieces;

    /**
     * transfer's statistics object
     */
    private Statistics stat = new Statistics();

    /**
     * piece requester
     */
    private PiecePicker picker;

    /**
     * peers selector
     */
    private Policy policy;


    private Session session;

    private boolean pause = false;
    private boolean abort = false;
    private HashSet<PeerConnection> connections = new HashSet<PeerConnection>();

    /**
     * session time when new peers request will be executed
     */
    private long nextTimeForSourcesRequest = 0;

    /**
     * session time when new peers request to KAD will be executed
     */
    private long nextTimeForDhtSourcesRequest = 0;

    /**
     * disk io
     */
    private PieceManager pm = null;

    /**
     * async disk io futures
     */
    LinkedList<Future<AsyncOperationResult> > aioFutures = new LinkedList<Future<AsyncOperationResult>>();

    /**
     * hashes of file's pieces
     */
    ArrayList<Hash> hashSet = new ArrayList<Hash>();

    /**
     * true if transfer has new state which wasn't written
     */
    private boolean needSaveResumeData = false;

    private TransferStatus.TransferState state = TransferStatus.TransferState.LOADING_RESUME_DATA;

    private PieceBlock lastResumeBlock = null;

    private SpeedMonitor speedMon = new SpeedMonitor(30);

    private boolean released = false;

    public Transfer(Session s, final AddTransferParams atp) throws JED2KException {
        assert(s != null);
        this.hash = atp.getHash();
        this.createTime = atp.getCreateTime().longValue();
        this.size = atp.getSize().longValue();
        assert(hash != null);
        assert(size != 0);
        numPieces = Utils.divCeil(this.size, Constants.PIECE_SIZE).intValue();
        int blocksInLastPiece = Utils.divCeil(size % Constants.PIECE_SIZE, Constants.BLOCK_SIZE).intValue();
        session = s;
        log.debug("created transfer {} dataSize {}", this.hash, this.size);

        pause = (atp.getPaused().byteValue() != 0);
        // create piece picker always now
        picker = new PiecePicker(numPieces, blocksInLastPiece);
        policy = new Policy(this);
        pm = new PieceManager(atp.getHandler(), numPieces, blocksInLastPiece);

        if (atp.resumeData.haveData()) {
            restore(atp.resumeData.getData());
        } else {
            setState(TransferStatus.TransferState.DOWNLOADING);
            /**
             * on start new transfer we need to save resume data to avoid transfer lost if session will interrupted
             */
            needSaveResumeData = true;
        }

        session.pushAlert(new TransferAddedAlert(this.hash));
    }

    /**
     * for testing purposes only
     * @param atp transfer parameters
     * @param picker external picker
     */
    public Transfer(final AddTransferParams atp, final PiecePicker picker) {
        this.hash = atp.getHash();
        this.size = atp.getSize().longValue();
        numPieces = Utils.divCeil(this.size, Constants.PIECE_SIZE).intValue();
        this.session = null;
        this.picker = picker;
    }

    /**
     * restore transfer's state using saved resume data
     * set have pieces directly into picker
     * restore partial pieces using step by step: allocate buffer -> async restore -> precess result
     * @param rd resume data
     */
    void restore(final TransferResumeData rd) {
        setHashSet(this.hash, rd.hashes);

        for(int i = 0; i < rd.pieces.size(); ++i) {
            if (rd.pieces.getBit(i)) picker.restoreHave(i);
        }

        for(final PieceBlock b: rd.downloadedBlocks) {
            picker.downloadPiece(b.pieceIndex);
            ByteBuffer buffer = session.allocatePoolBuffer();
            if (buffer == null) {
                log.warn("{} have no enough buffers to restore transfer {} ",
                        session.bufferPool, b);
                return;
            }

            setState(TransferStatus.TransferState.LOADING_RESUME_DATA);
            lastResumeBlock = b;
            asyncRestoreBlock(b, buffer);
            //aioFutures.addLast(session.submitDiskTask(new AsyncRestore(this, b, size, buffer)));
        }

        if (isFinished()) setState(TransferStatus.TransferState.FINISHED);
    }

    public Hash hash() {
        return hash;
    }

    public long getCreateTime() {
        return createTime;
    }

    public long size() {
        return this.size;
    }

    public int numHave() {
        return (picker != null)?picker.numHave():numPieces;
    }

    public int numPieces() {
        return numPieces;
    }

    /**
     * TODO - possibly this method will be useful for upload mode, but now it is useless since isSeed is equal isFinished
     * transfer in seed mode - it has all pieces
     * @return true if all pieces had been downloaded
     */
    //boolean isSeed() {
    //    return (picker == null) || (picker.numHave() == picker.numPieces());
    //}

    /**
     *
     * @return true if transfer has all pieces
     */
    public boolean isFinished() {
        return (picker == null) || (picker.numHave() == picker.numPieces());
        //return numPieces() - picker.numHave() == 0;
    }

    void weHave(int pieceIndex) {
        assert(picker != null);
        picker.weHave(pieceIndex);
    }

    final boolean isPaused() {
        return pause;
    }

    final boolean isAborted() {
        return abort;
    }

    final boolean wantMorePeers() {
        return !isPaused() && !isFinished() && policy.numConnectCandidates() > 0;
    }

    void addStats(Statistics s) {
        stat.add(s);
    }

    final void addPeer(Endpoint endpoint, int sourceFlag) throws JED2KException {
        policy.addPeer(new Peer(endpoint, true, sourceFlag));
    }

    final void removePeerConnection(PeerConnection c) {
        policy.conectionClosed(c, Time.currentTime());
        c.setPeer(null);
        // TODO - can't remove peer from collection due to simultaneous collection modification exception
        //connections.remove(c);
    }

    public PeerConnection connectoToPeer(Peer peerInfo) throws JED2KException {
        peerInfo.setLastConnected(Time.currentTime());
        peerInfo.setNextConnection(0);
        PeerConnection c = PeerConnection.make(session, peerInfo.getEndpoint(), this, peerInfo);
        session.connections.add(c);
        connections.add(c);
        policy.setConnection(peerInfo, c);
        c.connect();
        return peerInfo.getConnection();
    }

    void attachPeer(PeerConnection c) throws JED2KException {
        if (isPaused()) throw new JED2KException(ErrorCode.TRANSFER_PAUSED);
        if (isAborted()) throw new JED2KException(ErrorCode.TRANSFER_ABORTED);
        if (isFinished()) throw new JED2KException(ErrorCode.TRANSFER_FINISHED);
        policy.newConnection(c);
        connections.add(c);
        session.connections.add(c);
    }

    public void callPolicy(Peer peerInfo, PeerConnection c) {
        policy.setConnection(peerInfo, c);
    }

    void disconnectAll(BaseErrorCode ec) {
        Iterator<PeerConnection> itr = connections.iterator();
        while(itr.hasNext()) {
            PeerConnection c = itr.next();
            c.close(ec);
            if (c.isDisconnecting()) itr.remove();  // TODO - do not remove by iterator, simply call clean
        }

        assert(connections.isEmpty());
    }

    boolean tryConnectPeer(long sessionTime) throws JED2KException {
        assert(wantMorePeers());
        return policy.connectOnePeer(sessionTime);
    }

	void secondTick(final Statistics accumulator, long tickIntervalMS) {
        if (!isPaused() && !isAborted() && !isFinished() && connections.isEmpty()) {

            if (nextTimeForSourcesRequest < Time.currentTime()) {
                log.debug("[transfer] request peers on server {}", hash);
                session.sendSourcesRequest(hash, size);
                nextTimeForSourcesRequest = Time.currentTime() + Time.minutes(1);
            }

            if (nextTimeForDhtSourcesRequest < Time.currentTime()) {
                log.debug("[transfer] request peers on KAD {}", hash);
                session.sendDhtSourcesRequest(hash, size, this);
                nextTimeForDhtSourcesRequest = Time.currentTime() + Time.minutes(10);
            }
        }

        Iterator<PeerConnection> itr = connections.iterator();
        while(itr.hasNext()) {
            PeerConnection c = itr.next();
            stat.add(c.statistics());
            c.secondTick(tickIntervalMS);
            if (c.isDisconnecting()) itr.remove();
        }

        accumulator.add(stat);
        stat.secondTick(tickIntervalMS);

        speedMon.addSample(stat.downloadRate());

        while(!aioFutures.isEmpty()) {
            Future<AsyncOperationResult> res = aioFutures.peek();
            if (!res.isDone()) break;

            try {
                res.get().onCompleted();
            } catch (InterruptedException e) {
                log.warn("second tick aio InterruptedException {}", e);
            } catch( ExecutionException e) {
                log.warn("second tick aio ExecutionException {}", e);
            }
            finally {
                aioFutures.poll();
            }
        }
    }

    public Statistics statistics() {
        return stat;
    }

    public PiecePicker getPicker() {
        return picker;
    }

    public final boolean hasPicker() {
        return picker != null;
    }

    public PieceManager getPieceManager() { return pm; }

    /**
     * completely stop transfer:
     * abort all connections
     * cancel all disk i/o operations
     * release file
     */
    void abort(boolean deleteFile) {
        log.debug("abort transfer {} file {}"
                , hash
                , deleteFile?"delete":"save");

        if (abort) return;
        abort = true;
        disconnectAll(ErrorCode.TRANSFER_ABORTED);

        // cancel all async operations
        for(Future<AsyncOperationResult> f: aioFutures) {
            f.cancel(false);
        }

        aioFutures.clear();
        aioFutures.addLast(session.submitDiskTask(new AsyncRelease(this, deleteFile)));
    }

    void pause() {
        pause = true;
        disconnectAll(ErrorCode.TRANSFER_PAUSED);
        needSaveResumeData = true;
        session.pushAlert(new TransferPausedAlert(hash));
    }

    void resume() {
        pause = false;
        needSaveResumeData = true;
        session.pushAlert(new TransferResumedAlert(hash));
    }

    void setHashSet(final Hash hash, final AbstractCollection<Hash> hs) {
        if (hashSet.isEmpty()) {
            log.debug("{} hash set received {}", hash(), hs.size());
            hashSet.addAll(hs);
            needSaveResumeData = true;
        }
    }

    void piecePassed(int pieceIndex) {
        boolean was_finished = (numPieces == numHave());
        log.debug("piece passed, was finished: {}", was_finished?"true":"false");
        weHave(pieceIndex);
        if (!was_finished && isFinished()) {
            finished();
        }
    }

    /**
     * call this method when transfer becomes "finished" to finalize downloading
     */
    void finished() {
        disconnectAll(ErrorCode.TRANSFER_FINISHED);
        // policy will know transfer is finished automatically via call isFinished on transfer
        // async release file
        setState(TransferStatus.TransferState.FINISHED);
        aioFutures.addLast(session.submitDiskTask(new AsyncRelease(this, false)));
        needSaveResumeData = true;
        session.pushAlert(new TransferFinishedAlert(hash()));
    }

    void onBlockWriteCompleted(final PieceBlock b, final List<ByteBuffer> buffers, final BaseErrorCode ec) {
        log.debug("block {} write completed: {} free buffers: {}",
                b, ec, (buffers!=null)?buffers.size():0);

        // return buffers to pool
        if (buffers != null) {
            for (ByteBuffer buffer : buffers) {
                session.bufferPool.deallocate(buffer, Time.currentTime());
            }

            buffers.clear();
        }

        if (ec == ErrorCode.NO_ERROR) {
            picker.markAsFinished(b);
            needSaveResumeData = true;
        } else {
            picker.abortDownload(b, null);  // state of block must be writing!
            session.pushAlert(new TransferDiskIOErrorAlert(hash, ec));
            pause();
        }

        // reached last piece block from resume data, switch state
        if (lastResumeBlock != null && lastResumeBlock.equals(b)) {
            setState(TransferStatus.TransferState.DOWNLOADING);
        }
    }

    void onPieceHashCompleted(final int pieceIndex, final Hash hash) {
        assert hash != null;
        assert hashSet.size() > pieceIndex;

        if (hash != null && (hashSet.get(pieceIndex).compareTo(hash) != 0)) {
            log.error("restore piece {} due to expected hash {} != {} was calculated"
                    , pieceIndex
                    , hashSet.get(pieceIndex)
                    , hash);
            picker.restorePiece(pieceIndex);
        }
        else {
            piecePassed(pieceIndex);
        }

        needSaveResumeData = true;
    }

    void onReleaseFile(final BaseErrorCode c, final List<ByteBuffer> buffers) {
        assert buffers != null;
        log.debug("release file completed {} release byte buffers count {}"
                , c
                , buffers.size());


        for (ByteBuffer buffer : buffers) {
            session.bufferPool.deallocate(buffer, Time.currentTime());
        }

        // self remove transfer from processing map
        session.transfers.remove(hash);
    }

    /**
     *
     * @return resume data for transfer restore
     */
    TransferResumeData resumeData() {
        TransferResumeData trd = new TransferResumeData();
        trd.hashes.assignFrom(hashSet);

        if (hasPicker()) {
            trd.pieces.resize(picker.numPieces());
            for(int i = 0; i < numPieces(); ++i) {
                if (picker.havePiece(i)) {
                    trd.pieces.setBit(i);
                }
            }

            List<DownloadingPiece> downloadingQueue = picker.getDownloadingQueue();
            for(final DownloadingPiece dp: downloadingQueue) {
                for(int j = 0; j < dp.getBlocksCount(); ++j) {
                    if (dp.isFinished(j)) trd.downloadedBlocks.add(new PieceBlock(dp.pieceIndex, j));
                }
            }
        }

        // temporary do not save peers
        needSaveResumeData = false;
        return trd;
    }

    public File getFile() {
        return pm.getFile();
    }

    void setState(final TransferStatus.TransferState state) {
        if (this.state == state) return;
        this.state = state;
    }

    public void getBytesDone(final TransferStatus status) {
        status.totalWanted = size();
        status.totalDone = numHave()*Constants.PIECE_SIZE; //, status.totalWanted);
        int lastPiece = numPieces() - 1;

        // if we have last piece - correct total done since last piece size possibly is not equals whole piece size
        if (picker.havePiece(lastPiece)) {
            int corr = (int)(size() % Constants.PIECE_SIZE - Constants.PIECE_SIZE);
            assert corr <= 0;
            status.totalDone += corr;
        }

        int blocksInLastPiece = Utils.divCeil(size % Constants.PIECE_SIZE, Constants.BLOCK_SIZE).intValue();
        PieceBlock lastBlock = new PieceBlock(numPieces - 1, blocksInLastPiece - 1);

        List<DownloadingPiece> dq = picker.getDownloadingQueue();
        for(final DownloadingPiece dp: dq) {
            // skip have pieces since we are already calculated them
            if (picker.havePiece(dp.pieceIndex)) continue;
            status.totalDone += dp.downloadedCount()*Constants.BLOCK_SIZE;

            int corr = 0;
            if (dp.pieceIndex == lastBlock.pieceIndex && dp.isDownloaded(lastBlock.pieceBlock)) {
                corr = lastBlock.size(size) - Constants.BLOCK_SIZE_INT;
            }

            assert corr <= 0;

            status.totalDone += corr;
        }
    }

    public TransferStatus getStatus() {
        TransferStatus status = new TransferStatus();
        getBytesDone(status);

        status.paused = isPaused();
        status.downloadPayload = stat.totalPayloadDownload();
        status.downloadProtocol = stat.totalProtocolDownload();
        status.downloadRate = (int)stat.downloadRate();
        status.downloadPayloadRate = (int)stat.downloadPayloadRate();
        status.upload = stat.totalUpload();
        status.uploadRate = (int)stat.uploadRate();

        if (status.totalWanted == 0)        {
            status.progressPPM = 1000000;
            status.progress = 1.f;
        }
        else {
            status.progressPPM = (int)(status.totalDone * 1000000 / status.totalWanted);
            status.progress = ((float)status.totalDone)/status.totalWanted;
        }

        status.pieces = new BitField(numPieces());

        if (hasPicker()) {
            status.numPeers = policy.size();
            status.pieces = new BitField(picker.numPieces());

            for (int i = 0; i != picker.numPieces(); ++i) {
                if (picker.havePiece(i)) status.pieces.setBit(i);
            }

            status.numPieces = picker.numHave();

            long averageSpeed = speedMon.averageSpeed();
            if (averageSpeed != SpeedMonitor.INVALID_SPEED) {
                if (averageSpeed == 0) {
                    status.eta = SpeedMonitor.INVALID_ETA;
                } else {
                    status.eta = (status.totalWanted - status.totalDone) / averageSpeed;
                }
            }
        }
        else {
            status.pieces.setAll();
        }

        return status;
    }

    public void asyncRestoreBlock(final PieceBlock b, final ByteBuffer buffer) {
        aioFutures.addLast(session.submitDiskTask(new AsyncRestore(this, b, size, buffer)));
    }

    public final List<PeerInfo> getPeersInfo() {
        List<PeerInfo> res = new ArrayList<>();
        for(final PeerConnection c: connections) {
            // add peers only active peers
            if (c.statistics().downloadPayloadRate() > 0) res.add(c.getInfo());
        }

        return res;
    }

    boolean isNeedSaveResumeData() {
        return needSaveResumeData;
    }
}
