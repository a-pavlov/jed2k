package org.jed2k.protocol.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.jed2k.protocol.Unsigned.uint16;
import static org.jed2k.protocol.Unsigned.uint32;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.jed2k.protocol.ProtocolException;
import org.jed2k.protocol.Serializable;
import org.jed2k.protocol.ServerIdChange;
import org.jed2k.protocol.SoftSerializable;
import org.jed2k.protocol.UInt16;
import org.jed2k.protocol.UInt32;
import org.junit.Test;

public class SoftSerializableTest {
    private static byte[] source = {0x01, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x03, 0x00, 0x00, 0x00, 0x01, 0x00 };
    
    @Test
    public void testSoftSerialization() throws ProtocolException {
        ByteBuffer bb = ByteBuffer.wrap(source);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        Serializable sig = new ServerIdChange();
        assertTrue(sig instanceof SoftSerializable);
        SoftSerializable soft = (SoftSerializable)sig;
        soft.get(bb, 4);
        assertEquals(0x01, ((ServerIdChange)sig).clientId);
        assertEquals(0x00, ((ServerIdChange)sig).tcpFlags);
        assertEquals(0x00, ((ServerIdChange)sig).auxPort);
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
    public void testSoftSerializationMiddle() throws ProtocolException {
        ByteBuffer bb = ByteBuffer.wrap(source);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        Serializable sig = new ServerIdChange();
        assertTrue(sig instanceof SoftSerializable);
        SoftSerializable soft = (SoftSerializable)sig;
        soft.get(bb, 8);
        assertEquals(0x01, ((ServerIdChange)sig).clientId);
        assertEquals(0x02, ((ServerIdChange)sig).tcpFlags);
        assertEquals(0x00, ((ServerIdChange)sig).auxPort);
        UInt32 u32 = uint32();
        u32.get(bb);        
        assertEquals(0x03, u32.intValue());
        UInt16 u16 = uint16();
        u16.get(bb);
        assertEquals(0x01, u16.intValue());
        assertEquals(0, bb.remaining());
    }
    
    @Test
    public void testSoftSerializationFull() throws ProtocolException {
        ByteBuffer bb = ByteBuffer.wrap(source);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        Serializable sig = new ServerIdChange();
        assertTrue(sig instanceof SoftSerializable);
        SoftSerializable soft = (SoftSerializable)sig;
        soft.get(bb, 12);
        assertEquals(0x01, ((ServerIdChange)sig).clientId);
        assertEquals(0x02, ((ServerIdChange)sig).tcpFlags);
        assertEquals(0x03, ((ServerIdChange)sig).auxPort);        
        UInt16 u16 = uint16();
        u16.get(bb);
        assertEquals(0x01, u16.intValue());
        assertEquals(0, bb.remaining());
    }
}
