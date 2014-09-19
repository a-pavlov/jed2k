package org.jed2k.protocol.test;

import static junit.framework.Assert.assertEquals;
import static org.jed2k.protocol.Unsigned.uint8;

import java.nio.ByteBuffer;

import org.jed2k.protocol.ByteContainer;
import org.jed2k.protocol.UInt8;
import org.junit.Test;
import org.jed2k.protocol.NetworkBuffer;

public class ByteContainerTest{
    
    @Test
    public void testSerialize(){
        byte[] source = { (byte)0x07,
                (byte)0x20, (byte)0x20, (byte)0x30, (byte)0x31, (byte)0x32, (byte)0x20, (byte)0x20};
        NetworkBuffer nb = new NetworkBuffer(ByteBuffer.wrap(source));
        ByteContainer<UInt8> bc = new ByteContainer<UInt8>(uint8());
        bc.get(nb);
        assertEquals(7, bc.size.intValue());
        assertEquals(new String("  012  "), bc.toString());
    }
}