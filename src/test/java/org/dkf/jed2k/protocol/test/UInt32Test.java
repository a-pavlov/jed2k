package org.dkf.jed2k.protocol.test;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.UInt32;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.dkf.jed2k.protocol.Unsigned.uint32;

public class UInt32Test{

    @Test
    public void initialTest(){
        assertEquals(uint32(10), uint32(10));
        assertEquals(uint32((byte)0x10), uint32(0x10));
        assertEquals(uint32(0xffff), uint32(0xffff));
        UInt32 value = uint32();
        assertTrue(value.intValue() == 0);
        value.assign((short)1000);
        assertTrue(value.intValue() == 1000);
        value.assign((short)0xffff);
        assertEquals(-1, value.intValue());
    }

    @Test
    public void compareTest() {
        assertTrue(uint32(0xffff).compareTo(uint32(0xfff0)) == 1);
        assertTrue(uint32(0xf0f0).compareTo(uint32(0xf0f0)) == 0);
        assertTrue(uint32(0x0fff).compareTo(uint32(0xfff0)) == -1);
    }

    @Test
    public void serializationTest() throws JED2KException {
        byte[] source = { 0x03, 0x00, 0x00, 0x00 };
        ByteBuffer nb = ByteBuffer.wrap(source);
        nb.order(ByteOrder.LITTLE_ENDIAN);
        UInt32 u = uint32();
        u.get(nb);
        assertEquals(3, u.intValue());
    }
}