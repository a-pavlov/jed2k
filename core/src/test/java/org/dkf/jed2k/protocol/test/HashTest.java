package org.dkf.jed2k.protocol.test;

import org.dkf.jed2k.Constants;
import org.dkf.jed2k.Pair;
import org.dkf.jed2k.Utils;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.hash.MD4;
import org.dkf.jed2k.protocol.Hash;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HashTest{
    private final Logger log = LoggerFactory.getLogger(HashTest.class);
    private final boolean notAndroidPlatform = !System.getProperty("java.runtime.name").toLowerCase().startsWith("android");

    private final byte[] terminal = {
            (byte)0x31, (byte)0xD6, (byte)0xCF, (byte)0xE0,
            (byte)0xD1, (byte)0x6A, (byte)0xE9, (byte)0x31,
            (byte)0xB7, (byte)0x3C, (byte)0x59, (byte)0xD7,
            (byte)0xE0, (byte)0xC0, (byte)0x89, (byte)0xC0};

    @Test
    public void testInitialization() {
        Hash h = new Hash();
        assertEquals(h, Hash.INVALID);
        assertEquals(h, Hash.fromString("00000000000000000000000000000000"));
        assertEquals(Hash.TERMINAL, Hash.fromBytes(terminal));
        assertEquals("31D6CFE0D16AE931B73C59D7E0C089C0", Hash.TERMINAL.toString());
    }

    @Test
    public void testElementarySerialization() throws JED2KException {
        ByteBuffer nb = ByteBuffer.wrap(terminal);
        nb.order(ByteOrder.LITTLE_ENDIAN);
        Hash h = new Hash();
        h.get(nb);
        assertEquals(Hash.TERMINAL, h);

        ByteBuffer buff = ByteBuffer.allocate(MD4.HASH_SIZE);
        buff.order(ByteOrder.LITTLE_ENDIAN);
        Hash.LIBED2K.put(buff);
        buff.flip();
        Hash h2 = new Hash();
        h2.get(buff);
        assertEquals(Hash.LIBED2K, h2);
    }

    @Test
    public void testCompare() {
        Hash h = new Hash();
        assertEquals(0, h.compareTo(h));
        assertEquals(-1, h.compareTo(Hash.TERMINAL));
        assertEquals(1, Hash.fromString("10000000000000000000000000000000").compareTo(Hash.fromString("0FFFFFFFF00000000000000000000CCC")));
    }

    @Test
    public void testHashing() {
        LinkedList<Pair<Long, Hash>> llh = new LinkedList<Pair<Long, Hash>>();
        llh.push(Pair.make(100L, Hash.fromString("1AA8AFE3018B38D9B4D880D0683CCEB5")));
        llh.push(Pair.make(Constants.PIECE_SIZE, Hash.fromString("E76BADB8F958D7685B4549D874699EE9")));
        llh.push(Pair.make(Constants.PIECE_SIZE+1, Hash.fromString("49EC2B5DEF507DEA73E106FEDB9697EE")));
        if (notAndroidPlatform) llh.push(Pair.make(Constants.PIECE_SIZE*4, Hash.fromString("9385DCEF4CB89FD5A4334F5034C28893")));

        Iterator<Pair<Long, Hash>> itr = llh.iterator();
        while(itr.hasNext()) {
            Pair<Long, Hash> p = itr.next();
            byte[] src = new byte[p.left.intValue()];
            for(int i = 0; i < p.left.intValue(); ++i) {
                src[i] = 'X';
            }

            assertEquals(p.left.intValue(), src.length);

            Long pieces = Utils.divCeil(p.left, Constants.PIECE_SIZE);
            assertTrue(pieces.compareTo(0L) == 1);
            LinkedList<Hash> part_hashset = new LinkedList<Hash>();
            Long capacity = p.left;
            MD4 hasher = new MD4();

            for (int i = 0; i < pieces; ++i) {
                long in_piece_capacity = Math.min(Constants.PIECE_SIZE, capacity);

                while(in_piece_capacity > 0) {
                    int current_size = (int)Math.min(Constants.BLOCK_SIZE, in_piece_capacity);
                    hasher.update(src, (int)(p.left - capacity), current_size);
                    capacity -= current_size;
                    in_piece_capacity -= current_size;
                }

                part_hashset.add(Hash.fromBytes(hasher.digest()));
            }

            assertEquals(pieces.intValue(), part_hashset.size());

            if (pieces*Constants.PIECE_SIZE == p.left) {
                part_hashset.add(Hash.TERMINAL);
            }

            assertEquals(p.right, Hash.fromHashSet(part_hashset));
        }
    }

    @Test
    public void smallSelfTest() {
        byte data[] = new byte[1001];
        Arrays.fill(data, (byte)22);
        MD4 md4 = new MD4();
        md4.update(data);

        ByteBuffer bb = ByteBuffer.wrap(data);
        assertEquals(data.length, bb.remaining());
        MD4 md4b = new MD4();
        md4b.update(bb);
        assertEquals(Hash.fromBytes(md4.digest()), Hash.fromBytes(md4b.digest()));
    }

    @Test
    public void mediumSelfTest() {
        MD4 md4 = new MD4();
        MD4 md4b = new MD4();

        for(int i = 0; i < 10; ++i) {
            byte [] data = new byte[33 + i*10];
            for(int j = 0; j < data.length; ++j) {
                data[j] = (byte)i;
            }

            ByteBuffer buffer = ByteBuffer.wrap(data);
            assertEquals(buffer.remaining(), data.length);
            md4.update(data, 0, data.length);
            md4b.update(buffer);
            assertFalse(buffer.hasRemaining());
        }

        assertEquals(Hash.fromBytes(md4.digest()), Hash.fromBytes(md4b.digest()));
    }

    @Test
    public void testByteBufferHashing() {
        LinkedList<Pair<Long, Hash>> llh = new LinkedList<Pair<Long, Hash>>();
        llh.push(Pair.make(100L, Hash.fromString("1AA8AFE3018B38D9B4D880D0683CCEB5")));
        llh.push(Pair.make(Constants.PIECE_SIZE, Hash.fromString("E76BADB8F958D7685B4549D874699EE9")));
        llh.push(Pair.make(Constants.PIECE_SIZE+1, Hash.fromString("49EC2B5DEF507DEA73E106FEDB9697EE")));
        if (notAndroidPlatform) llh.push(Pair.make(Constants.PIECE_SIZE*4, Hash.fromString("9385DCEF4CB89FD5A4334F5034C28893")));

        Iterator<Pair<Long, Hash>> itr = llh.iterator();
        while(itr.hasNext()) {
            Pair<Long, Hash> p = itr.next();
            ByteBuffer src = ByteBuffer.allocate(p.left.intValue());
            for(int i = 0; i < p.left.intValue(); ++i) {
                src.put((byte)'X');
            }

            src.flip();

            assertEquals(p.left.intValue(), src.remaining());

            Long pieces = Utils.divCeil(p.left, Constants.PIECE_SIZE);
            assertTrue(pieces.compareTo(0L) == 1);
            LinkedList<Hash> part_hashset = new LinkedList<Hash>();
            Long capacity = p.left;
            MD4 hasher = new MD4();

            for (int i = 0; i < pieces; ++i) {
                long inPieceCapacity = Math.min(Constants.PIECE_SIZE, capacity);

                while(inPieceCapacity > 0) {
                    int currentSize = (int)Math.min(Constants.BLOCK_SIZE, inPieceCapacity);
                    int currentOffset = (int)(p.left - capacity);
                    ByteBuffer current = src.duplicate();
                    current.clear();
                    assertEquals(p.left.intValue(), current.capacity());
                    current.position(currentOffset).limit(currentSize + currentOffset);
                    assertEquals(currentSize, current.remaining());
                    hasher.update(current);
                    assertFalse(current.hasRemaining());
                    capacity -= currentSize;
                    inPieceCapacity -= currentSize;
                }

                part_hashset.add(Hash.fromBytes(hasher.digest()));
            }

            assertEquals(pieces.intValue(), part_hashset.size());

            if (pieces*Constants.PIECE_SIZE == p.left) {
                part_hashset.add(Hash.TERMINAL);
            }

            assertEquals(p.right, Hash.fromHashSet(part_hashset));
        }
    }

    @Test
    public void testHashInMap() {
        Map<Hash, Integer> m = new ConcurrentHashMap<>();
        m.put(Hash.fromString("1AA8AFE3018B38D9B4D880D0683CCEB5"), 1);
        Hash h = Hash.fromString("1AA8AFE3018B38D9B4D880D0683CCEB5");
        assertTrue(m.containsKey(h));
        m.remove(h);
        assertTrue(m.isEmpty());
    }

    @Test
    public void testEmuleHash() {
        Hash h = Hash.random(true);
        String str = h.toString();
        assertEquals("0E", str.substring(10, 12));
        assertEquals("6F", str.substring(28, 30));
    }
}
