package org.jed2k.protocol.test;

import static junit.framework.Assert.assertEquals;
import static org.jed2k.protocol.Unsigned.uint8;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.jed2k.protocol.ByteContainer;
import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.UInt32;
import org.jed2k.protocol.UInt8;
import org.junit.Test;

import static org.jed2k.protocol.Unsigned.uint32;

public class ByteContainerTest{
    
    @Test
    public void testSerialize() throws JED2KException{
        byte[] source = { (byte)0x07,
                (byte)0x20, (byte)0x20, (byte)0x30, (byte)0x31, (byte)0x32, (byte)0x20, (byte)0x20};
        ByteBuffer nb = ByteBuffer.wrap(source);
        nb.order(ByteOrder.LITTLE_ENDIAN);
        ByteContainer<UInt8> bc = new ByteContainer<UInt8>(uint8());
        bc.get(nb);
        assertEquals(7, bc.size.intValue());
        assertEquals(new String("  012  "), bc.toString());
    }
    
    @Test
    public void testSerialize2() throws JED2KException {
        byte[] source = { (byte)0x07, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x20, (byte)0x20, (byte)0x30, (byte)0x31, (byte)0x32, (byte)0x20, (byte)0x20};
        ByteBuffer nb = ByteBuffer.wrap(source);
        nb.order(ByteOrder.LITTLE_ENDIAN);
        ByteContainer<UInt32> bc = new ByteContainer<UInt32>(uint32());
        bc.get(nb);
        assertEquals(7, bc.size.intValue());
        assertEquals(new String("  012  "), bc.toString());
    }
}