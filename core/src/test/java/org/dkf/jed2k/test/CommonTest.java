package org.dkf.jed2k.test;

import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.Constants;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@Slf4j
public class CommonTest {
    private static byte[] data = new byte[(int)Constants.PIECE_SIZE];
    private static ByteBuffer dataBuffer = ByteBuffer.wrap(data);

    static {
        for(int i = 0; i < (int)Constants.PIECE_SIZE; ++i) {
            data[i] = 0;
        }
    }

    private ByteBuffer getBlock(int index) {
        assert(index < Constants.BLOCKS_PER_PIECE);
        dataBuffer.limit((int)(Constants.BLOCK_SIZE*(index+1)));
        dataBuffer.position((int)(index*Constants.BLOCK_SIZE));
        return dataBuffer.slice();
    }

    @Test
    public void testBufferwindows() {
        ByteBuffer piece = ByteBuffer.wrap(data);
        assertEquals((int)Constants.PIECE_SIZE, piece.limit());
        assertEquals((int)Constants.PIECE_SIZE, piece.remaining());

        ArrayList<Integer> template = new ArrayList<Integer>(Constants.BLOCKS_PER_PIECE);
        for (int i = 0; i < Constants.BLOCKS_PER_PIECE; ++i) {
            template.add(null);
        }

        template.set(4, 4);
        template.set(33, 3);
        template.set(12, 6);
        template.set(49, 1);
        template.set(0, 10);

        for(int i = 0; i < template.size(); ++i) {
            if (template.get(i) == null) continue;
            ByteBuffer block = getBlock(i);
            assertTrue(block != null);
            assertEquals((int)Constants.BLOCK_SIZE, block.remaining());
            for(int j = 0; j < (int)Constants.BLOCK_SIZE; ++j) {
                block.put((byte)template.get(i).intValue());
            }
        }

        for(int i = 0; i < (int)Constants.PIECE_SIZE; ++i) {
            assertEquals((template.get(i/(int)Constants.BLOCK_SIZE) != null)?template.get(i/(int)Constants.BLOCK_SIZE):0, data[i]);
        }
    }

    @Test
    public void testJavaNumbersConversion() {
        long original = 0xffffffffL;
        assertTrue(original > 0);
        int converted = (int)original;
        assertTrue(converted < 0);
        long converted2 = (long)(converted) & 0xffffffffL;
        assertEquals(0xffffffffL, converted2);
    }

    @Test
    public void testJavaIterator() {
        List<Integer> list = new LinkedList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        Iterator<Integer> itr = list.iterator();
        Integer i = null;
        while(itr.hasNext()) {
            i = itr.next();
            if (i.intValue() == 3) {
                itr.remove();
                break;
            }

            i = null;
        }

        assertEquals(3, i.intValue());
    }

    @Test
    public void testJavaStringSplitBehaviour() {
        assertEquals("1", "1".split("\\s+")[0]);
        assertEquals("1", "1   ".split("\\s+")[0]);
        assertEquals("3", "3 2 3".split("\\s+")[0]);
        assertEquals("455", "   455   567".trim().split("\\s+")[0]);
        assertEquals("", "  ".trim().split("\\s+")[0]);
    }

    @Test
    public void testLinkedHashMapFeatures() {
        LinkedHashMap<Integer, Integer> m = new LinkedHashMap<>(100, 100, true);
        for(int i = 0; i < 10; ++i) {
            m.put(i,i);
        }

        int i = 0;
        for(final int v: m.values()) {
            assertEquals(i++, v);
        }

        assertTrue(m.containsValue(9));
        assertTrue(m.containsValue(8));

        i = 0;
        for(final int v: m.values()) {
            assertEquals(i++, v);
        }

        Integer val = m.get(5);
        Integer val2 = m.get(8);
        assertEquals(5, val.intValue());
        assertEquals(8, val2.intValue());

        int template[] = {0, 1, 2, 3, 4, 6, 7, 9, 5, 8};
        i = 0;
        for(final int v: m.values()) {
            assertEquals(template[i++], v);
        }
    }
}
