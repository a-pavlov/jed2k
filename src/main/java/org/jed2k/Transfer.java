package org.jed2k;

import org.jed2k.exception.ErrorCode;
import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.Hash;
import org.jed2k.protocol.NetworkIdentifier;

import java.util.HashSet;

public class Transfer {
    private Hash hash;
    private long size;
    private Statistics stat = new Statistics();
    private PiecePicker picker;
    private Policy policy;
    private Session session;
    private boolean pause = false;
    private boolean abort = false;
    private HashSet<PeerConnection> connections = new HashSet<PeerConnection>();

    public Transfer(Session s, final AddTransferParams atp) {
        assert(s != null);
        assert(hash != null);
        assert(size != 0);
        this.hash = atp.hash;
        this.size = atp.size.longValue();
        session = s;

        // prepare piece picker here
        // in common case this is not correct condition(below)
        if (atp.resumeData == null) {
            picker = new PiecePicker(Utils.divCeil(this.size, Constants.PIECE_SIZE).intValue(),
                    Utils.divCeil(size % Constants.PIECE_SIZE, Constants.BLOCK_SIZE).intValue());
        }

        policy = new Policy(this);
    }

    Hash hash() {
        return hash;
    }

    public long size() {
        return this.size;
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

    final void addPeer(NetworkIdentifier endpoint) {
        policy.add(new Peer(endpoint, true));
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
        // TODO Auto-generated method stub
        stat.secondTick(currentSessionTime);
        // TODO - add statistics from all peed connections
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

    /**
     * transfer in seed mode - has all pieces
     * @return true if all pieces had been downloaded
     */
    public final boolean isSeed() {
        return (picker == null) || (picker.numHave() == picker.numPieces());
    }
}
