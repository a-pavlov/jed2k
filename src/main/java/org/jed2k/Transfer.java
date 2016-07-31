package org.jed2k;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import org.jed2k.protocol.Hash;
import org.jed2k.protocol.NetworkIdentifier;
import org.jed2k.data.PieceBlock;

public class Transfer {
    private Hash hash;
    private Set<NetworkIdentifier> sources = new TreeSet<NetworkIdentifier>();
    private long size;
    private ArrayList<Hash> hashset;
    private Statistics stat = new Statistics();
    private PiecePicker picker;
    private Policy policy;
    private Session session;
    private boolean pause = false;
    private boolean abort = false;

    public Transfer(Session s, final AddTransferParams atp) {
        assert(s != null);
        assert(hash != null);
        assert(size != 0);
        this.hash = atp.hash;
        this.size = atp.size.longValue();
        this.hashset = null;
        session = s;

        // prepare piece picker here
        // in common case this is not correct condition(below)
        if (atp.resumeData == null) {
            picker = new PiecePicker(Utils.divCeil(this.size, Constants.PIECE_SIZE).intValue(),
                    Utils.divCeil(size % Constants.PIECE_SIZE, Constants.BLOCK_SIZE).intValue());
        }

        policy = new Policy();
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

    public boolean validateHashset(Collection<Hash> hashset) {
        if (this.hashset != null) {
            // compare
        } else {
            return hash.equals(Hash.fromHashSet(hashset));
        }

        return true;
    }

    public PieceBlock requestBlock() {
        return new PieceBlock(0,0);
    }

    public void append(PeerConnection connection) {

    }

    void setupSources(Collection<NetworkIdentifier> sources) {
        for(NetworkIdentifier entry: sources) {
            if (!this.sources.contains(entry)) {
                this.sources.add(entry);
                // process new source
            }
        }
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

    void abort() {
        abort = true;
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
