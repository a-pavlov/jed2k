package org.jed2k.protocol.test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.logging.Logger;

import org.jed2k.Utils;
import org.jed2k.protocol.ByteContainer;
import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.BitField;
import org.junit.Test;

public class BitFieldTest {
    private static Logger log = Logger.getLogger(BitFieldTest.class.getName());
    
    @Test
    public void initializationTest() {
        BitField empty = new BitField();
        assertTrue(empty.empty());
        BitField bf = new BitField(10);
        assertEquals(0, bf.count());
        assertEquals(2, bf.bytes().length);
        BitField bf2 = new BitField(5, true);
        assertEquals(5, bf2.count());
        assertEquals(1, bf2.bytes().length);
        bf2.clearAll();
        assertEquals(0, bf2.count());
        bf2.setAll();
        assertEquals(bf2.size(), bf2.count());
        bf2.clearBit(0);
        assertEquals(bf2.size()-1, bf2.count());
        assertFalse(bf2.getBit(0));
        bf2.setBit(0);
        assertTrue(bf2.getBit(0));
        
        byte[] content = { (byte)0, (byte)1, (byte)0xf0 };
        BitField bfc = new BitField();
        bfc.assign(content, 20);
        assertEquals(3, bfc.bytes().length);
        assertEquals(5, bfc.count());
    }
    
    @Test
    public void advancedUsageTest() {
        byte[] content = { (byte)7, (byte)11, (byte)16 };
        BitField bf = new BitField(content, 22);
        assertEquals(7, bf.count());
        bf.resize(24, true);
        assertEquals(9, bf.count());
        assertEquals(3, bf.bytes().length);
        bf.resize(20, false);
        assertEquals(7, bf.count());
        assertEquals(3, bf.bytes().length);
        
        bf.resize(25, false);
        assertEquals(4, bf.bytes().length);
        assertEquals(7, bf.count());
    }
    
    @Test
    public void iteratorTest() {
        byte[] data = { (byte)8, (byte)7, (byte)10 };
        boolean[] template = {
                    false, false, false, false, true, false, false, false,
                    false, false, false, false, false, true, true, true,
                    false, false, false, false, true, false, true, false };        
        BitField bf = new BitField(data, 3*8);
        Iterator<Boolean> itr = bf.iterator();
        int index = 0;
        while(itr.hasNext()) {
            assertTrue(itr.next().compareTo(template[index++]) == 0);
        }
        
        assertEquals(template.length, index);
    }
}
