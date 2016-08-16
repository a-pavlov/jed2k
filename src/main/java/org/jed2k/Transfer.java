package org.jed2k;

import org.jed2k.data.PieceBlock;
import org.jed2k.exception.BaseErrorCode;
import org.jed2k.exception.ErrorCode;
import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.Hash;
import org.jed2k.protocol.NetworkIdentifier;

import java.nio.ByteBuffer;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Transfer {
    private Logger log = LoggerFactory.getLogger(Transfer.class);
    private Hash hash;
    private long size;
    private int numPieces;
    private Statistics stat = new Statistics();
    private PiecePicker picker;
    private Policy policy;
    private Session session;
    private boolean pause = false;
    private boolean abort = false;
    private HashSet<PeerConnection> connections = new HashSet<PeerConnection>();
    private long nextTimeForSourcesRequest = 0;
    PieceManager pm = null;
    LinkedList<Future<AsyncOperationResult> > aioFutures = new LinkedList<Future<AsyncOperationResult>>();
    ArrayList<Hash> hashSet = new ArrayList<Hash>();

    public Transfer(Session s, final AddTransferParams atp) {
        assert(s != null);
        this.hash = atp.hash;
        this.size = atp.size.longValue();
        assert(hash != null);
        assert(size != 0);
        numPieces = Utils.divCeil(this.size, Constants.PIECE_SIZE).intValue();
        session = s;
        log.debug("created transfer {} size {}", this.hash, this.size);

        // prepare piece picker here
        // in common case this is not correct condition(below)
        //if (atp.resumeData == null) {
            picker = new PiecePicker(Utils.divCeil(this.size, Constants.PIECE_SIZE).intValue(),
                    Utils.divCeil(size % Constants.PIECE_SIZE, Constants.BLOCK_SIZE).intValue());
        //}

        policy = new Policy(this);
        pm = new PieceManager(atp.filepath, Utils.divCeil(this.size, Constants.PIECE_SIZE).intValue(),
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
     * transfer in seed mode - it has all pieces
     * @return true if all pieces had been downloaded
     */
    boolean isSeed() {
        return (picker == null) || (picker.numHave() == picker.numPieces());
    }

    boolean isFinished() {
        if (isSeed()) return true;
        return numPieces() - picker.numHave() == 0;
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
        return !isPaused() && !isSeed() && policy.numConnectCandidates() > 0;
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
        connections.remove(c);
    }

    public PeerConnection connectoToPeer(Peer peerInfo) throws JED2KException {
        peerInfo.lastConnected = Time.currentTime();
        peerInfo.nextConnection = 0;
        PeerConnection c = PeerConnection.make(session, peerInfo.endpoint, this, peerInfo);
        session.connections.add(c);
        connections.add(c);
        policy.setConnection(peerInfo, c);
        c.connect();
        return peerInfo.connection;
    }

    void attachPeer(PeerConnection c) throws JED2KException {
        policy.newConnection(c);
    }

    public void callPolicy(Peer peerInfo, PeerConnection c) {
        policy.setConnection(peerInfo, c);
    }

    void disconnectAll() {
        for(PeerConnection c: connections) {
            c.close(ErrorCode.TRANSFER_ABORTED);
        }

        connections.clear();
    }

    boolean tryConnectPeer(long sessionTime) throws JED2KException {
        assert(wantMorePeers());
        return policy.connectOnePeer(sessionTime);
    }

	void secondTick(long currentSessionTime) {
        if (nextTimeForSourcesRequest < currentSessionTime && !isPaused() && !isAborted() && connections.isEmpty()) {
            log.debug("Request peers {}", hash);
            session.sendSourcesRequest(hash, size);
            nextTimeForSourcesRequest = currentSessionTime + 1000*60;   // one request per second
        }

        stat.secondTick(currentSessionTime);
        // TODO - fix this temp solution to avoid ConcurrentModification exception
        HashSet<PeerConnection> localc = new HashSet<PeerConnection>();
        localc = (HashSet<PeerConnection>)connections.clone();

        for(PeerConnection c: localc) {
            c.secondTick(currentSessionTime);
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
        if (abort) return;
        abort = true;
        disconnectAll();
    }

    void pause() {
        pause = true;
    }

    void resume() {
        pause = false;
    }

    void setHashSet(final Hash hash, final AbstractCollection<Hash> hs) {
        // TODO - add few checks here
        // 1. check common hash is compatible with hash set
        // 2. check hash set size
        // 3. compare new hash set and previous?
        // now copy first hash set to transfer
        if (hashSet.isEmpty()) {
            log.debug("{} hash set received {}", hash(), hs.size());
            hashSet.addAll(hs);
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

    void finished() {
        log.debug("transfer finished");
        // disconnect all here
        // mark policy is finished
        // async release file
        // alert transfer finished here
    }

    void onBlockWriteCompleted(final PieceBlock b, final LinkedList<ByteBuffer> buffers, final BaseErrorCode ec) {
        log.debug("block write completed: {} free buffers: {}, calculated hash: {}", b, buffers.size(), hash!=null?hash.toString():"null");

        // return buffers to pool
        for(ByteBuffer buffer: buffers) {
            session.bufferPool.deallocate(buffer, Time.currentTime());
        }

        buffers.clear();

        picker.markAsFinished(b);
    }

    void onPieceHashCompleted(final int pieceIndex, final Hash hash) {
        assert(hash != null);

        if (hash != null && (hashSet.get(pieceIndex).compareTo(hash) != 0)) {
            log.debug("restore piece due to unmatched hash");
            picker.restorePiece(pieceIndex);
        }
        else {
            piecePassed(pieceIndex);
        }
    }
}
