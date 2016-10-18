package org.dkf.jed2k.protocol.test;

import org.dkf.jed2k.protocol.UInt16;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.dkf.jed2k.protocol.Unsigned.uint16;

public class UInt16Test{

    @Test
    public void initialTest(){
        assertEquals(uint16(10), uint16(10));
        assertEquals(uint16((byte)0x10), uint16(0x10));
        assertEquals(uint16(0xffff), uint16(0xffff));
        UInt16 value = uint16();
        assertTrue(value.intValue() == 0);
        value.assign((short)1000);
        assertTrue(value.intValue() == 1000);
        value.assign((short)0xffff);
        assertTrue(value.intValue() == 0xffff);
    }

    @Test
    public void compareTest(){
        assertTrue(uint16(0xffff).compareTo(uint16(0xfff0)) == 1);
        assertTrue(uint16(0xf0f0).compareTo(uint16(0xf0f0)) == 0);
        assertTrue(uint16(0x0fff).compareTo(uint16(0xfff0)) == -1);
    }
}