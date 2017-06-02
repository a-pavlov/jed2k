package org.dkf.jed2k.protocol.test;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.BitField;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BitFieldTest {

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
    public void cleanupTailTest() {
        byte[] content = { (byte)0, (byte)0xff };
        BitField bf = new BitField(content, 12);
        assertEquals(4, bf.count());
        assertTrue(bf.getBit(11));
        assertTrue(bf.getBit(10));
        assertTrue(bf.getBit(9));
        assertTrue(bf.getBit(8));
        assertFalse(bf.getBit(6));
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

    @Test
    public void serializeGetTest() throws JED2KException {
        byte[] data = { 0x0c, 0x00, 0x0f, 0x70 };
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        BitField empty = new BitField();
        empty.get(bb);
        boolean[] template = {false, false, false, false, true, true, true, true, false, true, true, true };
        Iterator<Boolean> itr = empty.iterator();
        int i = 0;
        while(itr.hasNext()) {
            assertTrue(itr.next().compareTo(template[i++]) == 0);
        }

        assertEquals(i, empty.size());

        byte[] wholeData = { 0x00, 0x00 };
        bb = ByteBuffer.wrap(wholeData);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        BitField wholeBf = new BitField();
        wholeBf.get(bb);
        assertTrue(wholeBf.empty());
        assertTrue(wholeBf.bytes() != null);
        assertEquals(0, wholeBf.bytes().length);
    }

    @Test
    public void serializePutTest() throws JED2KException {
        ByteBuffer bb = ByteBuffer.allocate(10);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        BitField empty = new BitField();
        empty.put(bb);
        bb.flip();
        assertEquals(2, bb.remaining());
        assertEquals(0, bb.getShort());
        bb.clear();

        byte[] data = { (byte)0x08, (byte)0x07, (byte)0x0a, (byte)0xff };
        BitField bfd = new BitField(data, 28);
        bfd.put(bb);
        bb.flip();
        assertEquals(28, bb.getShort());
        assertEquals(0xf00a0708, bb.getInt());
    }

    @Test
    public void testGetPut() throws JED2KException {
        ByteBuffer bb = ByteBuffer.allocate(20);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        BitField bf1 = new BitField(10);
        bf1.setBit(1);
        bf1.setBit(2);
        bf1.setBit(9);
        assertEquals(10, bf1.size());
        bf1.put(bb);
        bb.flip();
        BitField bf2 = new BitField();
        bf2.get(bb);
        //assertFalse(bb.hasRemaining());
        assertEquals(bf1, bf2);
    }

    @Test
    public void testGetPutResize() throws JED2KException {
        BitField bf1 = new BitField();
        bf1.resize(10);
        ByteBuffer bb = ByteBuffer.allocate(bf1.bytesCount());
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bf1.setBit(1);
        bf1.setBit(2);
        bf1.setBit(9);
        assertEquals(10, bf1.size());
        bf1.put(bb);
        assertFalse(bb.hasRemaining());
        bb.flip();
        BitField bf2 = new BitField();
        bf2.get(bb);
        assertFalse(bb.hasRemaining());
        assertEquals(bf1, bf2);
    }
}
