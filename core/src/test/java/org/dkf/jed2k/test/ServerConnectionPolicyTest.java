package org.dkf.jed2k.test;

import org.dkf.jed2k.Pair;
import org.dkf.jed2k.ServerConnectionPolicy;
import org.junit.Test;

import java.net.InetSocketAddress;

import static org.junit.Assert.*;

public class ServerConnectionPolicyTest {

    @Test
    public void testServerConenctionPolicyTrivial() {
        ServerConnectionPolicy scp = new ServerConnectionPolicy(1, 2);
        assertNull(scp.getConnectCandidate(1));
        scp.setServerConnection("123", new InetSocketAddress("192.168.0.9", 1223), 4);
        assertNull(scp.getConnectCandidate(4));
        Pair<String, InetSocketAddress> cc = scp.getConnectCandidate(4 + 1000 + 1);
        assertEquals(Pair.make("123", new InetSocketAddress("192.168.0.9", 1223)), cc);
        scp.setServerConnection("123", new InetSocketAddress("192.168.0.9", 1223), 0);
        assertNull(scp.getConnectCandidate(1000));
        Pair<String, InetSocketAddress> cc2 = scp.getConnectCandidate(2000 + 1);
        assertEquals(Pair.make("123", new InetSocketAddress("192.168.0.9", 1223)), cc2);
        scp.setServerConnection("123", new InetSocketAddress("192.168.0.9", 1223), 0);
        assertNull(scp.getConnectCandidate(4000));
        scp.clear();
        assertNull(scp.getConnectCandidate(2999));
        scp.setServerConnection("123", new InetSocketAddress("192.168.0.9", 1223), 0);
        assertNotNull(scp.getConnectCandidate(1000 + 1));
    }
}
