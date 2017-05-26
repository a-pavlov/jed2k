package org.dkf.jed2k;

import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Endpoint;

import java.util.*;

/**
 * peers management class - choose peer for connection
 * extends AbstractCollection for unit testing purposes
 */
@Slf4j
public class Policy extends AbstractCollection<Peer> {
    public static final int MAX_PEER_LIST_SIZE = 100;
    public static final int MIN_RECONNECT_TIMEOUT = 10;
    private int roundRobin = 0;
    private ArrayList<Peer> peers = new ArrayList<Peer>();
    private Transfer transfer = null;
    private Random rnd = new Random();

    public Policy(Transfer t) {
        transfer = t;
    }

    public boolean isConnectCandidate(final Peer pe) {
        assert(pe != null);
        // TODO - use fail count parameter here
        return !(pe.hasConnection() || !pe.isConnectable() || pe.getFailCount() > 10);
    }

    public boolean isEraseCandidate(Peer pe) {
        if (pe.hasConnection() || isConnectCandidate(pe)) return false;
        return pe.getFailCount() > 0;
    }

    Peer get(Endpoint endpoint) {
        for(Peer p: peers) {
            if (p.getEndpoint().equals(endpoint)) return p;
        }

        return null;
    }

    public boolean addPeer(Peer p) throws JED2KException {
        assert(p != null);

        if (MAX_PEER_LIST_SIZE != 0 && peers.size() >= MAX_PEER_LIST_SIZE) {
            erasePeers();
            if (peers.size() >= MAX_PEER_LIST_SIZE) throw new JED2KException(ErrorCode.PEER_LIMIT_EXEEDED);
        }

        int insertPos = Collections.binarySearch(peers, p);

        // update peer source flag
        if (insertPos >= 0) {
            peers.get(insertPos).setSourceFlag(peers.get(insertPos).getSourceFlag() | p.getSourceFlag());
            return false;
        }

        peers.add(((insertPos + 1)*-1), p);
        return true;
    }

    /**
     * try to remove some peers from list
     */
    public void erasePeers() {

        if (MAX_PEER_LIST_SIZE == 0 || peers.isEmpty()) return;

        int eraseCandidate = -1;

        int roundRobin = rnd.nextInt(peers.size());
        assert roundRobin >=0;
        assert roundRobin < peers.size();

        int lowWatermark = MAX_PEER_LIST_SIZE * 95 / 100;
        if (lowWatermark == MAX_PEER_LIST_SIZE) --lowWatermark;

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
                    assert current >= 0;
                    assert current < peers.size();
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
        if (lhs.getFailCount() != rhs.getFailCount())
            return lhs.getFailCount() < rhs.getFailCount();

        // Local peers should always be tried first
        boolean lhsLocal = Utils.isLocalAddress(lhs.getEndpoint());
        boolean rhsLocal = Utils.isLocalAddress(rhs.getEndpoint());
        if (lhsLocal != rhsLocal) {
            return lhsLocal;
        }

        if (lhs.getLastConnected() != rhs.getLastConnected())
            return lhs.getLastConnected() < rhs.getLastConnected();


        if (lhs.getNextConnection() != rhs.getNextConnection())
            return lhs.getNextConnection() < rhs.getNextConnection();

        int lhsRank = getSourceRank(lhs.getSourceFlag());
        int rhsRank = getSourceRank(rhs.getSourceFlag());
        if (lhsRank != rhsRank) return lhsRank > rhsRank;

        return false;
    }

    /**
     *
     * @param lhs - Peer
     * @param rhs - Peer
     * @return true if lhs better erase candidate than rhs
     */
    boolean comparePeerErase(Peer lhs, Peer rhs) {
        assert(!lhs.hasConnection());
        assert(!rhs.hasConnection());

        // primarily, prefer getting rid of peers we've already tried and failed
        if (lhs.getFailCount() != rhs.getFailCount())
            return lhs.getFailCount() > rhs.getFailCount();

        boolean lhsResumeDataSource = (lhs.getSourceFlag() & PeerInfo.RESUME) == PeerInfo.RESUME;
        boolean rhsResumeDataSource = (rhs.getSourceFlag() & PeerInfo.RESUME) == PeerInfo.RESUME;

        // prefer to drop peers whose only source is resume data
        if (lhsResumeDataSource != rhsResumeDataSource) {
            return lhsResumeDataSource;
        }

        if (lhs.connectable != rhs.connectable) {
            return !lhs.connectable;
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
            if (roundRobin >= peers.size()) roundRobin = 0;
            Peer pe = peers.get(roundRobin);
            assert(pe != null);
            int current = roundRobin;

            if (peers.size() > MAX_PEER_LIST_SIZE) {
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
            if (pe.getNextConnection() != 0 && pe.getNextConnection() < sessionTime) continue;
            // 10 seconds timeout for each fail
            if (pe.getLastConnected() != 0 && (sessionTime < pe.getLastConnected() + Time.seconds(pe.getFailCount() + 1)*MIN_RECONNECT_TIMEOUT)) continue;
            candidate = current;
        }

        if (eraseCandidate != -1) {
            if (candidate > eraseCandidate) --candidate;
            peers.remove(eraseCandidate);
        }

        if (candidate == -1) return null;
        return peers.get(candidate);
    }


    public boolean connectOnePeer(long sessionTime) throws JED2KException {
        Peer peerInfo = findConnectCandidate(sessionTime);
        if (peerInfo != null) {
            log.debug("[policy] connect peer {}", peerInfo);
            assert isConnectCandidate(peerInfo);
            assert peerInfo.isConnectable();
            transfer.connectoToPeer(peerInfo);
            return peerInfo.hasConnection();
        }

        return false;
    }

    public void conectionClosed(PeerConnection c, long sessionTime) {
        Peer p = c.getPeer();
        if (p == null) return;
        p.setConnection(null);
        p.setLastConnected(sessionTime);
        if (c.isFailed()) p.setFailCount(p.getFailCount() + 1);
        if (!p.isConnectable()) peers.remove(p);
    }

    public void setConnection(Peer p, PeerConnection c) {
        assert(c != null);
        assert(p != null);
        p.setConnection(c);
    }

    private boolean shouldEraseImmediately(Peer p) {
        return (p.getSourceFlag() & PeerInfo.RESUME) == PeerInfo.RESUME;
    }

    @Override
    public Iterator<Peer> iterator() {
        return peers.iterator();
    }

    @Override
    public int size() {
        return peers.size();
    }

    /**
     * slow version of connect candidates counter O(n)
     * @return
     */
    public int numConnectCandidates() {
        int res = 0;
        if (!transfer.isFinished()) {
            for (final Peer p : peers) {
                if (isConnectCandidate(p) && !isEraseCandidate(p)) ++res;
            }
        }
        return res;
    }

    public void newConnection(PeerConnection c) throws JED2KException {
        Peer p = get(c.getEndpoint());

        if (p != null) {
            // some actions here
            if (p.hasConnection()) {
                // stupid, but simply throw exception here
                throw new JED2KException(ErrorCode.DUPLICATE_PEER_CONNECTION);
            }
        }
        else {
            p = new Peer(c.getEndpoint(), false, 0);
            if (!addPeer(p)) {
                throw new JED2KException(ErrorCode.DUPLICATE_PEER);
            }
        }

        assert(p != null);
        p.setConnection(c);
        c.setPeer(p);
    }

    public int getSourceRank(int sourceBitmask) {
        int ret = 0;
        if (Utils.isBit(sourceBitmask, PeerInfo.SERVER)) ret |= 1 << 5;
        if (Utils.isBit(sourceBitmask, PeerInfo.DHT)) ret |= 1 << 4;
        if (Utils.isBit(sourceBitmask, PeerInfo.INCOMING)) ret |= 1 << 3;
        if (Utils.isBit(sourceBitmask, PeerInfo.RESUME)) ret |= 1 << 2;
        return ret;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("policy transfer: ").append(transfer!=null?transfer.hash().toString():"?").append(" peers: ");
        for(final Peer p: peers) {
            sb.append(p.toString());
        }

        return sb.toString();
    }

    public Peer findPeer(final Endpoint ep) {
        int pos = Collections.binarySearch(peers, new Peer(ep));
        if (pos >= 0) return peers.get(pos);
        return null;
    }
}

