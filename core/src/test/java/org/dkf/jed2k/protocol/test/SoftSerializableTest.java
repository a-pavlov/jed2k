package org.dkf.jed2k.protocol.test;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.SoftSerializable;
import org.dkf.jed2k.protocol.UInt16;
import org.dkf.jed2k.protocol.UInt32;
import org.dkf.jed2k.protocol.server.IdChange;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.dkf.jed2k.protocol.Unsigned.uint16;
import static org.dkf.jed2k.protocol.Unsigned.uint32;

public class SoftSerializableTest {
    private static byte[] source = {0x01, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x03, 0x00, 0x00, 0x00, 0x01, 0x00 };

    @Test
    public void testSoftSerialization() throws JED2KException {
        ByteBuffer bb = ByteBuffer.wrap(source);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        Serializable sig = new IdChange();
        assertTrue(sig instanceof SoftSerializable);
        SoftSerializable soft = (SoftSerializable)sig;
        soft.get(bb, 4);
        assertEquals(0x01, ((IdChange)sig).clientId);
        assertEquals(0x00, ((IdChange)sig).tcpFlags);
        assertEquals(0x00, ((IdChange)sig).auxPort);
        UInt32 u32 = uint32();
        u32.get(bb);
        assertEquals(0x02, u32.intValue());
        u32.get(bb);
        assertEquals(0x03, u32.intValue());
        UInt16 u16 = uint16();
        u16.get(bb);
        assertEquals(0x01, u16.intValue());
        assertEquals(0, bb.remaining());
    }

    @Test
    public void testSoftSerializationMiddle() throws JED2KException {
        ByteBuffer bb = ByteBuffer.wrap(source);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        Serializable sig = new IdChange();
        assertTrue(sig instanceof SoftSerializable);
        SoftSerializable soft = (SoftSerializable)sig;
        soft.get(bb, 8);
        assertEquals(0x01, ((IdChange)sig).clientId);
        assertEquals(0x02, ((IdChange)sig).tcpFlags);
        assertEquals(0x00, ((IdChange)sig).auxPort);
        UInt32 u32 = uint32();
        u32.get(bb);
        assertEquals(0x03, u32.intValue());
        UInt16 u16 = uint16();
        u16.get(bb);
        assertEquals(0x01, u16.intValue());
        assertEquals(0, bb.remaining());
    }

    @Test
    public void testSoftSerializationFull() throws JED2KException {
        ByteBuffer bb = ByteBuffer.wrap(source);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        Serializable sig = new IdChange();
        assertTrue(sig instanceof SoftSerializable);
        SoftSerializable soft = (SoftSerializable)sig;
        soft.get(bb, 12);
        assertEquals(0x01, ((IdChange)sig).clientId);
        assertEquals(0x02, ((IdChange)sig).tcpFlags);
        assertEquals(0x03, ((IdChange)sig).auxPort);
        UInt16 u16 = uint16();
        u16.get(bb);
        assertEquals(0x01, u16.intValue());
        assertEquals(0, bb.remaining());
    }
}
