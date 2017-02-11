package org.dkf.jed2k.protocol.test;

import org.dkf.jed2k.ResourceFile;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.server.ServerMet;
import org.junit.Assume;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Created by ap197_000 on 06.09.2016.
 */
public class ServerMetTest {

    @Test
    public void testSerialization() throws JED2KException {
        Assume.assumeTrue(!System.getProperty("java.runtime.name").toLowerCase().startsWith("android"));
        ResourceFile rf = new ResourceFile();
        ByteBuffer buffer = rf.read("server.met", null);
        assertTrue(buffer.hasRemaining());
        ServerMet sm = new ServerMet();
        sm.get(buffer);
        assertFalse(sm.getServers().isEmpty());
    }

    @Test
    public void testSerialization2() throws JED2KException {
        ResourceFile rf = new ResourceFile();
        ByteBuffer buffer = rf.read("server2.met", null);
        assertTrue(buffer.hasRemaining());
        ServerMet sm = new ServerMet();
        sm.get(buffer);
        assertFalse(sm.getServers().isEmpty());
    }

    @Test
    public void testGetters() throws JED2KException {
        ServerMet sm = new ServerMet();
        sm.addServer(ServerMet.ServerMetEntry.create(new Endpoint(new InetSocketAddress("192.168.0.9", 1223)).getIP(), (short)5600, "Test server name", "Test descr"));
        sm.addServer(ServerMet.ServerMetEntry.create("mule.org", (short)45567, "Name2", null));
        ByteBuffer bb = ByteBuffer.allocate(sm.bytesCount());
        bb.order(ByteOrder.LITTLE_ENDIAN);
        sm.put(bb);
        ServerMet sm2 = new ServerMet();
        bb.flip();
        sm2.get(bb);
        assertFalse(bb.hasRemaining());
        assertEquals("Test server name", sm2.getServers().get(0).getName());
        assertEquals("Test descr", sm2.getServers().get(0).getDescription());
        assertEquals("192.168.0.9", sm2.getServers().get(0).getHost());

        assertEquals("Name2", sm2.getServers().get(1).getName());
        assertEquals("", sm2.getServers().get(1).getDescription());
        assertEquals("mule.org", sm2.getServers().get(1).getHost());
    }
}