package org.jed2k.protocol.test;

import static junit.framework.Assert.assertEquals;
import static org.jed2k.protocol.Unsigned.uint8;

import java.nio.ByteBuffer;

import org.jed2k.protocol.ByteContainer;
import org.jed2k.protocol.ProtocolException;
import org.jed2k.protocol.UInt32;
import org.jed2k.protocol.UInt8;
import org.junit.Test;
import org.jed2k.protocol.NetworkBuffer;

import static org.jed2k.protocol.Unsigned.uint32;

public class ByteContainerTest{
    
    @Test
    public void testSerialize() throws ProtocolException{
        byte[] source = { (byte)0x07,
                (byte)0x20, (byte)0x20, (byte)0x30, (byte)0x31, (byte)0x32, (byte)0x20, (byte)0x20};
        NetworkBuffer nb = new NetworkBuffer(ByteBuffer.wrap(source));
        ByteContainer<UInt8> bc = new ByteContainer<UInt8>(uint8());
        bc.get(nb);
        assertEquals(7, bc.size.intValue());
        assertEquals(new String("  012  "), bc.toString());
    }
    
    @Test
    public void testSerialize2() throws ProtocolException {
        byte[] source = { (byte)0x07, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x20, (byte)0x20, (byte)0x30, (byte)0x31, (byte)0x32, (byte)0x20, (byte)0x20};
        NetworkBuffer nb = new NetworkBuffer(ByteBuffer.wrap(source));
        ByteContainer<UInt32> bc = new ByteContainer<UInt32>(uint32());
        bc.get(nb);
        assertEquals(7, bc.size.intValue());
        assertEquals(new String("  012  "), bc.toString());
    }
}