package org.jed2k;

import org.jed2k.data.PieceBlock;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * peers management class - choose peer for connection
 */
public class Policy {
    private int roundRobin = 0;
    private ArrayList<Peer> peers = new ArrayList<Peer>();

    boolean isConnectCandidate(Peer pe) {
        assert(pe != null);
        // TODO - use fail count parameter here
        if (pe.connection != null || pe.failCount > 10) return false;
        return true;
    }

    boolean isEraseCandidate(Peer pe) {
        if (pe.connection != null || isConnectCandidate(pe)) return false;
        return pe.failCount > 0;
    }

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

        //int lhs_rank = source_rank(lhs.source);
        //int rhs_rank = source_rank(rhs.source);
        //if (lhs_rank != rhs_rank) return lhs_rank > rhs_rank;
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
                if (isEraseCandidate(pe)) {
                    eraseCandidate = current;
                }
            }

            ++roundRobin;
            if (!isConnectCandidate(pe) && comparePeers(peers.get(candidate), pe)) continue;
            if (pe.nextConnection != 0 && pe.nextConnection < sessionTime) continue;
            // TODO - use min reconnect time parameter here
            if (pe.lastConnected != 0 && sessionTime - pe.lastConnected < (pe.failCount + 1)*10) continue;
            candidate = current;
        }

        if (eraseCandidate != -1) peers.remove(eraseCandidate);
        if (candidate == -1) return null;
        return peers.get(candidate);
    }


    public boolean connectOnePeer(long sessionTime) {
        Peer pe = findConnectCandidate(sessionTime);
        if (pe != null) {
            // connect in transfer here
        }

        return false;
    }
}
