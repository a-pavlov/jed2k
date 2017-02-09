package org.dkf.jed2k.protocol.kad.test;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.kad.KadEndpoint;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Created by inkpot on 16.01.2017.
 */
public class KadEndpointTest {

    @Test
    public void testKadEndpointEquals() {
        assertEquals(new KadEndpoint(192, 10000, 20000), new KadEndpoint(192, 10000, 20000));
        assertFalse(new KadEndpoint(456, 5677, 5677).equals(new KadEndpoint(456, 5678, 5677)));
    }

    @Test
    public void testKadBackSerialization() throws JED2KException {
        KadEndpoint endpoint = new KadEndpoint(5667, 7890, 1234);
        ByteBuffer buffer = ByteBuffer.allocate(endpoint.bytesCount());
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        endpoint.put(buffer);
        assertFalse(buffer.hasRemaining());
        buffer.flip();
        KadEndpoint res = new KadEndpoint();
        res.get(buffer);
        assertFalse(buffer.hasRemaining());
        assertEquals(endpoint, res);
    }
}
