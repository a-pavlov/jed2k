package org.dkf.jed2k.protocol.test;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.UInt8;
import org.junit.Test;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.dkf.jed2k.protocol.Unsigned.uint8;

public class UInt8Test{

    @Test
    public void testValue(){
        UInt8 i1 = uint8(0);
        assertEquals(i1.byteValue(), uint8(0).byteValue());
    }

    @Test
    public void testComparable(){
        assertTrue(uint8(0xff).compareTo(uint8(0)) == 1);
        assertTrue(uint8(0xaa).compareTo(uint8(0xff)) == -1);
        assertTrue(uint8(250).compareTo(uint8(250)) == 0);
    }

    @Test
    public void testOverflow(){
        assertTrue(uint8(0xff0f).compareTo(uint8(0x0f)) == 0);
    }

    @Test(expected=BufferUnderflowException.class)
    public void testSerialize() throws JED2KException {
        byte[] data = { (byte)1, (byte)2, (byte)3 };
        ByteBuffer nb = ByteBuffer.wrap(data);
        nb.order(ByteOrder.LITTLE_ENDIAN);
        UInt8 value = uint8();
        value.get(nb);
        assertTrue(value.intValue() == 1);
        value.get(nb);
        assertTrue(value.intValue() == 2);
        value.get(nb);
        assertTrue(value.intValue() == 3);
        value.get(nb);
    }
}