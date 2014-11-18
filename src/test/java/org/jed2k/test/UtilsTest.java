package org.jed2k.test;

import static junit.framework.Assert.assertEquals;
import static org.jed2k.Utils.int2Address;

import org.jed2k.Constants;
import org.jed2k.Pair;
import org.jed2k.Utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;

import org.junit.Test;


public class UtilsTest {
    
    @Test
    public void testIpAddressConversion() throws UnknownHostException {        
        assertEquals(InetAddress.getByName("0.0.0.22"), int2Address(0x16));
        assertEquals(InetAddress.getByName("0.0.0.16"), int2Address(0x10));
        assertEquals(InetAddress.getByName("127.0.0.1"), int2Address(0x7f000001));
        assertEquals(InetAddress.getByName("196.127.10.1"), int2Address(0xc47f0a01));
    }
    
    @Test
    public void testFileCoords() {
        // <size, <pieces, blocks_in_last_piece>>
        LinkedList<Pair<Long, Pair<Integer, Integer>>> template = new LinkedList<Pair<Long, Pair<Integer, Integer>>>();
        template.add(Pair.make(100l, Pair.make(1, 1)));
        template.add(Pair.make(Constants.PIECE_SIZE, Pair.make(1, (int)(Constants.PIECE_SIZE/Constants.BLOCK_SIZE))));
        template.add(Pair.make(Constants.PIECE_SIZE*5+Constants.BLOCK_SIZE*10+100, Pair.make(5+1, (int)(10 + 1))));
        
        for(Pair<Long, Pair<Integer, Integer>> value: template) {
            int pieces = Utils.divCeil(value.left, (long)Constants.PIECE_SIZE).intValue();
            int blocksPerPiece = (Utils.divCeil(Constants.PIECE_SIZE, Constants.BLOCK_SIZE)).intValue();
            int blocksInLastPiece = Utils.divCeil(value.left % Constants.PIECE_SIZE, Constants.BLOCK_SIZE).intValue();
            if (blocksInLastPiece == 0) blocksInLastPiece = blocksPerPiece;
            assertEquals(50, blocksPerPiece);
            assertEquals(value.right.left.intValue(), pieces);
            assertEquals(value.right.right.intValue(), blocksInLastPiece);
        }
    }
}
