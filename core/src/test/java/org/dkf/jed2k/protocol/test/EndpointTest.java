package org.dkf.jed2k.protocol.test;


import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Endpoint;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class EndpointTest {
    @Test
    public void testSerialize() throws JED2KException {
        byte source[] = {
            (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00,  (byte)0x01, (byte)0x00
        };

        Endpoint ni = new Endpoint();
        ByteBuffer nb = ByteBuffer.wrap(source);
        nb.order(ByteOrder.LITTLE_ENDIAN);
        ni.get(nb);
        assertEquals(1, ni.getIP());
        assertEquals(1, ni.getPort());
    }

    @Test
    public void testConvertation() throws JED2KException {
        Endpoint endpoint = new Endpoint(new InetSocketAddress("192.168.0.9", 1223));
        assertEquals(new InetSocketAddress("192.168.0.9", 1223), endpoint.toInetSocketAddress());
        assertTrue(endpoint.defined());
        Endpoint endpoint1 = Endpoint.fromInet(new InetSocketAddress("xxx", 60077));
        assertFalse(endpoint1.defined());
        assertEquals(0, endpoint1.getIP());
        assertEquals(0, endpoint1.getPort());
    }

    @Test
    public void testLargePort() throws JED2KException {
        Endpoint endpoint = new Endpoint(0, 64000);
        ByteBuffer bb = ByteBuffer.allocate(endpoint.bytesCount());
        bb.order(ByteOrder.LITTLE_ENDIAN);
        endpoint.put(bb);
        Endpoint endpoint2 = new Endpoint();
        bb.flip();
        endpoint2.get(bb);
        assertEquals(64000, endpoint2.getPort());

    }
}

