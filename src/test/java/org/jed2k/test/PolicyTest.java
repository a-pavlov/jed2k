package org.jed2k.test;

import org.jed2k.protocol.NetworkIdentifier;
import org.jed2k.Peer;
import org.junit.Test;
import org.jed2k.Policy;

import java.net.InetSocketAddress;
import java.util.Iterator;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;

/**
 * Created by inkpot on 05.07.2016.
 */
public class PolicyTest {
    Peer p1 = new Peer(new NetworkIdentifier(new InetSocketAddress("192.168.0.1", 7081)));
    Peer p2 = new Peer(new NetworkIdentifier(new InetSocketAddress("192.168.0.2", 7082)));
    Peer p3 = new Peer(new NetworkIdentifier(new InetSocketAddress("192.168.0.3", 7083)));
    Peer p4 = new Peer(new NetworkIdentifier(new InetSocketAddress("192.168.0.4", 7084)));

    @Test
    public void testCandidates() {
        Policy p = new Policy();
        assertTrue(p.isConnectCandidate(p1));
        assertTrue(p.isConnectCandidate(p2));
        assertTrue(p.isConnectCandidate(p3));
        assertTrue(p.isConnectCandidate(p4));
        assertFalse(p.isEraseCandidate(p1));
        assertFalse(p.isEraseCandidate(p2));
        assertFalse(p.isEraseCandidate(p3));
        assertFalse(p.isEraseCandidate(p4));
    }

    @Test
    public void testInsertPeer() {
        Peer ps[] = {p1, p2, p3, p4};
        Policy p = new Policy();
        assertTrue(p.insertPeer(p1));
        assertFalse(p.insertPeer(p1));
        assertTrue(p.insertPeer(p4));
        assertTrue(p.insertPeer(p2));
        assertTrue(p.insertPeer(p3));
        assertEquals(4, p.size());
        Iterator<Peer> itr = p.iterator();
        int i = 0;
        while(itr.hasNext()) {
            assertEquals(ps[i++], itr.next());
        }
    }
}
