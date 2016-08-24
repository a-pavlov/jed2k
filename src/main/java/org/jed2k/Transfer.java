package org.jed2k;

import org.jed2k.alert.TransferDiskIOError;
import org.jed2k.alert.TransferFinishedAlert;
import org.jed2k.alert.TransferPausedAlert;
import org.jed2k.alert.TransferResumedAlert;
import org.jed2k.data.PieceBlock;
import org.jed2k.exception.BaseErrorCode;
import org.jed2k.exception.ErrorCode;
import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Transfer {
    private Logger log = LoggerFactory.getLogger(Transfer.class);

    /**
     * transfer's file hash
     */
    private Hash hash;

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
     * session time when new peers request will executed
     */
    private long nextTimeForSourcesRequest = 0;

    /**
     * disk io
     */
    PieceManager pm = null;

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

    public Transfer(Session s, final AddTransferParams atp) throws JED2KException {
        assert(s != null);
        this.hash = atp.hash;
        this.size = atp.size.longValue();
        assert(hash != null);
        assert(size != 0);
        numPieces = Utils.divCeil(this.size, Constants.PIECE_SIZE).intValue();
        session = s;
        log.debug("created transfer {} dataSize {}", this.hash, this.size);

        // prepare piece picker here
        // in common case this is not correct condition(below)
        //if (atp.resumeData == null) {
            picker = new PiecePicker(Utils.divCeil(this.size, Constants.PIECE_SIZE).intValue(),
                    Utils.divCeil(size % Constants.PIECE_SIZE, Constants.BLOCK_SIZE).intValue());
        //}

        policy = new Policy(this);
        pm = new PieceManager(atp.filepath.asString(), Utils.divCeil(this.size, Constants.PIECE_SIZE).intValue(),
                Utils.divCeil(size % Constants.PIECE_SIZE, Constants.BLOCK_SIZE).intValue());
    }

    Hash hash() {
        return hash;
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

    /**
     * request sources from server, kad, etc
     */
    final void requestSources() {
        // server request
        session.sendSourcesRequest(hash, size);
    }

    final void addPeer(NetworkIdentifier endpoint) throws JED2KException {
        policy.addPeer(new Peer(endpoint, true));
    }

    final void removePeerConnection(PeerConnection c) {
        policy.conectionClosed(c, Time.currentTime());
        c.setPeer(null);
        // TODO - can't remove peer from collection due to simultaneous collection modification exception
        //connections.remove(c);
    }

    public PeerConnection connectoToPeer(Peer peerInfo) throws JED2KException {
        peerInfo.lastConnected = Time.currentTime();
        peerInfo.nextConnection = 0;
        PeerConnection c = PeerConnection.make(session, peerInfo.endpoint, this, peerInfo);
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

	void secondTick(long currentSessionTime) {
        if (nextTimeForSourcesRequest < currentSessionTime && !isPaused() && !isAborted() && !isFinished() && connections.isEmpty()) {
            log.debug("Request peers {}", hash);
            session.sendSourcesRequest(hash, size);
            nextTimeForSourcesRequest = currentSessionTime + 1000*60;   // one request per second
        }

        stat.secondTick(currentSessionTime);

        Iterator<PeerConnection> itr = connections.iterator();
        while(itr.hasNext()) {
            PeerConnection c = itr.next();
            c.secondTick(currentSessionTime);
            if (c.isDisconnecting()) itr.remove();
        }

        while(!aioFutures.isEmpty()) {
            Future<AsyncOperationResult> res = aioFutures.peek();
            if (!res.isDone()) break;

            try {
                res.get().onCompleted();
            } catch (InterruptedException e) {
                // TODO - handle it
            } catch( ExecutionException e) {
                // TODO - handle it
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

    void abort() {
        log.debug("{} abort", hash);
        if (abort) return;
        abort = true;
        disconnectAll(ErrorCode.TRANSFER_ABORTED);

        // cancel all async operations
        for(Future<AsyncOperationResult> f: aioFutures) {
            f.cancel(false);
        }

        aioFutures.clear();
        aioFutures.addLast(session.diskIOService.submit(new AsyncRelease(this)));
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
        // TODO - add few checks here
        // 1. check common hash is compatible with hash set
        // 2. check hash set dataSize
        // 3. compare new hash set and previous?
        // now copy first hash set to transfer
        if (hashSet.isEmpty()) {
            log.debug("{} hash set received {}", hash(), hs.size());
            hashSet.addAll(hs);
            needSaveResumeData = true;
        }
    }

    void piecePassed(int pieceIndex) {
        boolean was_finished = (numPieces == numHave());
        log.debug("piece passed, was finsihed: {}", was_finished?"true":"false");
        weHave(pieceIndex);
        if (!was_finished && isFinished()) {
            finished();
        }
    }

    /**
     * call this method when transfer becomes "finished" to finalize downloading
     */
    void finished() {
        log.info("transfer {} finished", hash);
        disconnectAll(ErrorCode.TRANSFER_FINISHED);
        // policy will know transfer is finished automatically via call isFinished on transfer
        // async release file
        aioFutures.addLast(session.diskIOService.submit(new AsyncRelease(this)));
        session.pushAlert(new TransferFinishedAlert(hash()));
    }

    void onBlockWriteCompleted(final PieceBlock b, final LinkedList<ByteBuffer> buffers, final BaseErrorCode ec) {
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
            picker.abortDownload(b);
            session.pushAlert(new TransferDiskIOError(hash, ec));
            pause();
        }
    }

    void onPieceHashCompleted(final int pieceIndex, final Hash hash) {
        assert(hash != null);

        if (hash != null && (hashSet.get(pieceIndex).compareTo(hash) != 0)) {
            log.debug("restore piece due to expected hash {} is not equal with calculated {}",
                    hashSet.get(pieceIndex), hash);
            picker.restorePiece(pieceIndex);
        }
        else {
            piecePassed(pieceIndex);
        }

        needSaveResumeData = true;
    }

    void onReleaseFile(final BaseErrorCode c) {
        log.debug("release file completed {}", c);
    }

    /**
     *
     * @return resume data for transfer restore
     */
    TransferResumeData resumeData() {
        TransferResumeData trd = new TransferResumeData();
        trd.hashes.assignFrom(hashSet);

        if (hasPicker()) {
            for(int i = 0; i < numPieces(); ++i) {
                if (picker.havePiece(i)) {
                    trd.pieces.add(PieceResumeData.makeCompleted());
                    continue;
                }

                DownloadingPiece dp = picker.getDownloadingPiece(i);
                if (dp != null) {
                    PieceResumeData prd = new PieceResumeData(PieceResumeData.ResumePieceStatus.PARTIAL, new BitField(dp.getBlocksCount()));
                    Iterator<DownloadingPiece.BlockState> itr = dp.iterator();
                    int bitIndex = 0;
                    prd.blocks.clearAll();
                    while(itr.hasNext()) {
                        if (itr.next() == DownloadingPiece.BlockState.STATE_FINISHED) prd.blocks.setBit(bitIndex);
                        ++bitIndex;
                    }
                }
                else {
                    trd.pieces.add(PieceResumeData.makeEmpty());
                }
            }
        }

        // temporary do not save peers
        needSaveResumeData = false;
        return trd;
    }
}
