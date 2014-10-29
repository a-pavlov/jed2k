package org.jed2k.protocol.test;

import static junit.framework.Assert.assertEquals;

import java.util.logging.Logger;

import org.jed2k.Utils;
import org.jed2k.protocol.ByteContainer;
import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.BitField;
import org.junit.Test;

import static org.jed2k.protocol.Unsigned.uint32;

public class BitFieldTest {
    private static Logger log = Logger.getLogger(BitFieldTest.class.getName());
    
    
    @Test
    public void baseTesting() {
        byte[] content = { 0, 1, (byte)0xf0 };
        BitField bf = new BitField();
        bf.assign(content, 20);
        assertEquals(3, bf.bytes().length);
        log.info(Utils.byte2String(bf.bytes()));
        assertEquals(5, bf.count());
    }
}