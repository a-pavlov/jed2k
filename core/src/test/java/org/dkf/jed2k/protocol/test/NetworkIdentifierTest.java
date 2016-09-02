package org.dkf.jed2k.protocol.test;


import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.NetworkIdentifier;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static junit.framework.Assert.assertEquals;

public class NetworkIdentifierTest{
    @Test
    public void testSerialize() throws JED2KException {
        byte source[] = {
            (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00,  (byte)0x01, (byte)0x00
        };

        NetworkIdentifier ni = new NetworkIdentifier();
        ByteBuffer nb = ByteBuffer.wrap(source);
        nb.order(ByteOrder.LITTLE_ENDIAN);
        ni.get(nb);
        assertEquals(1, ni.getIP());
        assertEquals(1, ni.getPort());
    }

    @Test
    public void testConvertation() throws JED2KException {
        NetworkIdentifier endpoint = new NetworkIdentifier(new InetSocketAddress("192.168.0.9", 1223));
        assertEquals(new InetSocketAddress("192.168.0.9", 1223), endpoint.toInetSocketAddress());
    }
}

