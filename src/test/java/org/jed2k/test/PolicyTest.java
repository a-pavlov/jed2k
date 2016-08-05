package org.jed2k.test;

import org.jed2k.PeerConnection;
import org.jed2k.Transfer;
import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.NetworkIdentifier;
import org.jed2k.Peer;
import org.junit.Before;
import org.junit.Test;
import org.jed2k.Policy;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;

import java.net.InetSocketAddress;
import java.util.Iterator;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;

/**
 * Created by inkpot on 05.07.2016.
 */
public class PolicyTest {
    Peer p1 = new Peer(new NetworkIdentifier(new InetSocketAddress("192.168.0.1", 7081)), true);
    Peer p2 = new Peer(new NetworkIdentifier(new InetSocketAddress("192.168.0.2", 7082)), true);
    Peer p3 = new Peer(new NetworkIdentifier(new InetSocketAddress("192.168.0.3", 7083)), true);
    Peer p4 = new Peer(new NetworkIdentifier(new InetSocketAddress("192.168.0.4", 7084)), true);

    private Transfer transfer = null;
    private PeerConnection connection = null;

    @Before
    public void beforeEachTest() throws JED2KException {
        transfer = Mockito.mock(Transfer.class);
        connection = Mockito.mock(PeerConnection.class);
        when(transfer.connectoToPeer(any(Peer.class))).thenReturn(connection);
        Mockito.doCallRealMethod().when(transfer).callPolicy(any(Peer.class), any(PeerConnection.class));
    }

    @Test
    public void testCandidates() {
        Transfer t = Mockito.mock(Transfer.class);
        Policy p = new Policy(t);
        assertTrue(p.isConnectCandidate(p1));
        assertTrue(p.isConnectCandidate(p2));
        assertTrue(p.isConnectCandidate(p3));
        assertTrue(p.isConnectCandidate(p4));
        assertFalse(p.isEraseCandidate(p1));
        assertFalse(p.isEraseCandidate(p2));
        assertFalse(p.isEraseCandidate(p3));
        assertFalse(p.isEraseCandidate(p4));
    }

    @Test(expected=JED2KException.class)
    public void testDuplicateInsert() throws JED2KException {
        Transfer t = Mockito.mock(Transfer.class);
        Peer ps[] = {p1, p2, p3, p4};
        Policy p = new Policy(t);
        p.addPeer(p1);
        p.addPeer(p1);
    }

    @Test
    public void testInsertPeer() throws JED2KException {
        Transfer t = Mockito.mock(Transfer.class);
        Peer ps[] = {p1, p2, p3, p4};
        Policy p = new Policy(t);
        p.addPeer(p1);
        p.addPeer(p4);
        p.addPeer(p2);
        p.addPeer(p3);
        assertEquals(4, p.size());
        Iterator<Peer> itr = p.iterator();
        int i = 0;
        while(itr.hasNext()) {
            assertEquals(ps[i++], itr.next());
        }
    }

    @Test
    public void testConnectDisconnect() throws JED2KException {
        Policy p = Mockito.spy(new Policy(transfer));
        p.addPeer(p1);
        assertEquals(1, p.numConnectCandidates());
        assertTrue(p.findConnectCandidate(10L) != null);
    }

    @Test
    public void testNewConnection() throws JED2KException {
        Transfer t = Mockito.mock(Transfer.class);
        PeerConnection c = Mockito.mock(PeerConnection.class);
        when(c.getEndpoint()).thenReturn(new NetworkIdentifier(10, (short)4661));
        Policy p = new Policy(t);
        p.newConnection(c);
        assertEquals(1, p.size());
        PeerConnection c2 = Mockito.mock(PeerConnection.class);
        when(c2.getEndpoint()).thenReturn(new NetworkIdentifier(11, (short)6789));
        p.newConnection(c2);
        assertEquals(2, p.size());
        assertEquals(0, p.numConnectCandidates());
    }

    @Test(expected = JED2KException.class)
    public void testNewConnectionDuplicate() throws JED2KException {
        Transfer t = Mockito.mock(Transfer.class);
        PeerConnection c = Mockito.mock(PeerConnection.class);
        Policy p = new Policy(t);
        when(c.getEndpoint()).thenReturn(new NetworkIdentifier(10, (short)4661));
        PeerConnection c2 = Mockito.mock(PeerConnection.class);
        when(c2.getEndpoint()).thenReturn(new NetworkIdentifier(10, (short)4661));
        p.newConnection(c);
        p.newConnection(c2);
    }
}
