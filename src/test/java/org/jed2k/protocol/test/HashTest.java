package org.jed2k.protocol.test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;
import java.util.LinkedList;

import org.jed2k.hash.MD4;
import org.jed2k.protocol.Hash;
import org.jed2k.exception.JED2KException;
import org.jed2k.Pair;
import org.jed2k.Constants;
import org.jed2k.Utils;
import org.junit.Test;

public class HashTest{
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
        assertEquals(new String("31D6CFE0D16AE931B73C59D7E0C089C0"), Hash.TERMINAL.toString());
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
        ByteBuffer nbw = buff;
        Hash.LIBED2K.put(nbw);
        buff.flip();
        Hash h2 = new Hash();
        h2.get(nbw);
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
        llh.push(Pair.make(100l, Hash.fromString("1AA8AFE3018B38D9B4D880D0683CCEB5")));
        //llh.push(Pair.make(Constants.PIECE_SIZE, Hash.fromString("E76BADB8F958D7685B4549D874699EE9")));
        //llh.push(Pair.make(Constants.PIECE_SIZE+1, Hash.fromString("49EC2B5DEF507DEA73E106FEDB9697EE")));
        //llh.push(Pair.make(Constants.PIECE_SIZE*4, Hash.fromString("9385DCEF4CB89FD5A4334F5034C28893")));
        
        Iterator<Pair<Long, Hash>> itr = llh.iterator();
        while(itr.hasNext()) {
            Pair<Long, Hash> p = itr.next();
            byte[] src = new byte[p.left.intValue()];
            for(int i = 0; i < p.left.intValue(); ++i) {
                src[i] = 'X';
            }
            
            assertEquals(p.left.intValue(), src.length);
            
            MD4 full = new MD4();
            full.engineUpdate(src, 0, src.length);
            assertEquals(p.right, Hash.fromBytes(full.engineDigest()));
            /*
            
            Long pieces = Utils.divCeil(p.left, Constants.PIECE_SIZE);
            assertTrue(pieces.compareTo(0l) == 1);
            LinkedList<Hash> part_hashset = new LinkedList<Hash>();            
            Long capacity = p.left;
            MD4 hasher = new MD4();
            
            for (int i = 0; i < pieces; ++i) {
                long in_piece_capacity = Math.min(Constants.PIECE_SIZE, capacity);

                while(in_piece_capacity > 0) {
                    int current_size = (int)Math.min(Constants.BLOCK_SIZE, in_piece_capacity);
                    hasher.engineUpdate(src, (int)(p.left - capacity), current_size);
                    capacity -= current_size;
                    in_piece_capacity -= current_size;
                }

                part_hashset.push(Hash.fromBytes(hasher.engineDigest()));
            }
            
            assertEquals(pieces.intValue(), part_hashset.size());

            if (pieces*Constants.PIECE_SIZE == p.left) {
                part_hashset.push(Hash.TERMINAL);
            }
            
            assertEquals(p.right, Hash.fromHashSet(part_hashset));
            */
        }
    }
}
