package org.jed2k;

import org.jed2k.data.PieceBlock;

import java.util.*;

/**
 * peers management class - choose peer for connection
 * extends AbstractCollection for unit testing purposes
 */
public class Policy extends AbstractCollection<Peer> {
    private int roundRobin = 0;
    private ArrayList<Peer> peers = new ArrayList<Peer>();

    public boolean isConnectCandidate(Peer pe) {
        assert(pe != null);
        // TODO - use fail count parameter here
        if (pe.connection != null || !pe.isConnectable() || pe.failCount > 10) return false;
        return true;
    }

    public boolean isEraseCandidate(Peer pe) {
        if (pe.connection != null || isConnectCandidate(pe)) return false;
        return pe.failCount > 0;
    }

    public boolean insertPeer(Peer p) {
        assert(p != null);

        int maxPeerlistSize = 100;

        if (maxPeerlistSize != 0 && peers.size() >= maxPeerlistSize) {
            erasePeers();
            if (peers.size() >= maxPeerlistSize) return false;
        }

        int insertPos = Collections.binarySearch(peers, p);
        if (insertPos >= 0) return false;
        peers.add(((insertPos + 1)*-1), p);
        return true;
    }

    /**
     * try to remove some peers from list
     */
    private void erasePeers() {
        int maxPeerListSize = 100;

        if (maxPeerListSize == 0 || peers.isEmpty()) return;

        int eraseCandidate = -1;

        Random rnd = new Random();
        int roundRobin = rnd.nextInt() % peers.size();

        int lowWatermark = maxPeerListSize * 95 / 100;
        if (lowWatermark == maxPeerListSize) --lowWatermark;

        for (int iterations = Math.min(peers.size(), 300); iterations > 0; --iterations)
        {
            if (peers.size() < lowWatermark) break;
            if (roundRobin == peers.size()) roundRobin = 0;

            Peer pe = peers.get(roundRobin);
            int current = roundRobin;

            // check pe is erase candidate or we already have erase candidate and it not better than pe for erase
            if (isEraseCandidate(pe) && (eraseCandidate == -1 || !comparePeerErase(peers.get(eraseCandidate), pe))) {
                if (shouldEraseImmediately(pe)) {
                    if (eraseCandidate > current) eraseCandidate--;
                    peers.remove(current);
                } else eraseCandidate = current;
            }

            ++roundRobin;
        }

        if (eraseCandidate > -1)
        {
            assert(eraseCandidate >= 0 && eraseCandidate < peers.size());
            peers.remove(eraseCandidate);
        }
    }

    /**
     *
     * @param lhs
     * @param rhs
     * @return true if lhs better connect candidate than rhs
     */
    boolean comparePeers(Peer lhs, Peer rhs) {
        // prefer peers with lower failcount
        if (lhs.failCount != rhs.failCount)
            return lhs.failCount < rhs.failCount;

        // Local peers should always be tried first
        boolean lhs_local = Utils.isLocalAddress(lhs.endpoint);
        boolean rhs_local = Utils.isLocalAddress(rhs.endpoint);
        if (lhs_local != rhs_local) {
            if (lhs_local) return true;
            return false;
        }

        if (lhs.lastConnected != rhs.lastConnected)
            return lhs.lastConnected < rhs.lastConnected;


        if (lhs.nextConnection != rhs.nextConnection)
            return lhs.nextConnection < rhs.nextConnection;

        return false;
    }

    /**
     *
     * @param lhs - Peer
     * @param rhs - Peer
     * @return true if lhs better erase candidate than rhs
     */
    boolean comparePeerErase(Peer lhs, Peer rhs) {
        assert(lhs.connection == null);
        assert(rhs.connection == null);

        // primarily, prefer getting rid of peers we've already tried and failed
        if (lhs.failCount != rhs.failCount)
            return lhs.failCount > rhs.failCount;

        boolean lhs_resume_data_source = lhs.source == Peer.SourceFlag.SF_RESUME_DATA.value;
        boolean rhs_resume_data_source = rhs.source == Peer.SourceFlag.SF_RESUME_DATA.value;

        // prefer to drop peers whose only source is resume data
        if (lhs_resume_data_source != rhs_resume_data_source) {
            if (lhs_resume_data_source) return true;
            return false;
        }

        if (lhs.connectable != rhs.connectable) {
            if (!lhs.connectable) return true;
            return false;
        }

        return false;
    }

    /**
     *
     * @param sessionTime - current session time in seconds
     * @return candidate for connection or null if hadn't found candidates
     */
    public Peer findConnectCandidate(long sessionTime) {
        int candidate = -1;
        int eraseCandidate = -1;
        if (roundRobin >= peers.size()) roundRobin = 0;
        for(int iteration = 0; iteration < Math.min(peers.size(), 300); ++iteration) {
            if (roundRobin > peers.size()) roundRobin = 0;
            Peer pe = peers.get(iteration);
            assert(pe != null);
            int current = roundRobin;

            // TODO - use parameter here as max peer list size
            if (peers.size() > 100) {
                if (isEraseCandidate(pe) && (eraseCandidate == -1 || !comparePeerErase(peers.get(eraseCandidate), pe))) {
                    if (shouldEraseImmediately(pe)) {
                        if (eraseCandidate > current) --eraseCandidate;
                        if (candidate > current) --candidate;
                        peers.remove(current);
                        continue;
                    }
                    else eraseCandidate = current;
                }
            }

            ++roundRobin;
            if (!isConnectCandidate(pe)) continue;
            if (candidate != -1 && comparePeers(peers.get(candidate), pe)) continue;
            if (pe.nextConnection != 0 && pe.nextConnection < sessionTime) continue;
            // TODO - use min reconnect time parameter here
            if (pe.lastConnected != 0 && sessionTime - pe.lastConnected < (pe.failCount + 1)*10) continue;
            candidate = current;
        }

        if (eraseCandidate != -1) {
            if (candidate > eraseCandidate) --candidate;
            peers.remove(eraseCandidate);
        }

        if (candidate == -1) return null;
        return peers.get(candidate);
    }


    public boolean connectOnePeer(long sessionTime) {
        Peer pe = findConnectCandidate(sessionTime);
        if (pe != null) {
            assert(pe.isConnectable());
            // connect in transfer here
        }

        return false;
    }

    public void conectionClose(PeerConnection c, long sessionTime) {
        Peer p = c.getPeer();
        if (p == null) return;
        p.connection = null;
        p.lastConnected = sessionTime;
        if (c.isFailed()) p.failCount++;
        if (!p.isConnectable()) peers.remove(p);
    }

    private boolean shouldEraseImmediately(Peer p) {
        return p.source == Peer.SourceFlag.SF_RESUME_DATA.value;
    }

    @Override
    public Iterator<Peer> iterator() {
        return peers.iterator();
    }

    @Override
    public int size() {
        return peers.size();
    }
}