package org.dkf.jed2k.protocol.test;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Hash;
import org.dkf.jed2k.protocol.client.RequestParts32;
import org.dkf.jed2k.protocol.client.RequestParts64;
import org.dkf.jed2k.protocol.client.SendingPart32;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static junit.framework.Assert.*;

/**
 * Created by ap197_000 on 11.08.2016.
 */
public class PartsTest {

    @Test
    public void testRequestParts() throws JED2KException {
        ByteBuffer buffer = ByteBuffer.allocate(100);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        RequestParts32 rp = new RequestParts32(Hash.LIBED2K);
        assertFalse(rp.isFool());
        assertTrue(rp.isEmpty());
        rp.append(100, 200);
        rp.append(400, 500);
        rp.append(600, 800);
        assertTrue(rp.isFool());
        rp.put(buffer);
        buffer.flip();
        RequestParts32 rp_res = new RequestParts32();
        rp_res.get(buffer);
        assertTrue(rp_res.isFool());
        assertFalse(rp_res.isEmpty());
        assertEquals(Hash.LIBED2K, rp_res.getHash());
        assertEquals(100, rp_res.getBeginOffset(0).intValue());
        assertEquals(200, rp_res.getEndOffset(0).intValue());
        assertEquals(600, rp_res.getBeginOffset(2).intValue());
        assertEquals(800, rp_res.getEndOffset(2).intValue());
    }

    @Test
    public void testRequest64() throws JED2KException {
        ByteBuffer buffer = ByteBuffer.allocate(100);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        RequestParts64 rp = new RequestParts64(Hash.EMULE);
        rp.append(19999999999L, 29999999999L);
        rp.put(buffer);
        buffer.flip();
        RequestParts64 rp_res = new RequestParts64();
        rp_res.get(buffer);
        assertEquals(19999999999L, rp_res.getBeginOffset(0).longValue());
        assertEquals(29999999999L, rp_res.getEndOffset(0).longValue());
    }

    @Test
    public void testSendingPart() throws JED2KException {
        ByteBuffer buffer = ByteBuffer.allocate(100);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        SendingPart32 sp32 = new SendingPart32();
        sp32.hash.assign(Hash.EMULE);
        sp32.beginOffset.assign(100000);
        sp32.endOffset.assign(200000);
        sp32.put(buffer);
        buffer.flip();
        SendingPart32 sp32_res = new SendingPart32();
        sp32_res.get(buffer);
        assertEquals(Hash.EMULE, sp32_res.hash);
        assertEquals(100000, sp32_res.beginOffset.intValue());
        assertEquals(200000, sp32_res.endOffset.intValue());
    }
}
