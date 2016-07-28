package org.jed2k.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;
import static org.jed2k.Utils.int2Address;

import org.jed2k.Constants;
import org.jed2k.Pair;
import org.jed2k.Utils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;

import org.jed2k.protocol.NetworkIdentifier;
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

    @Test
    public void testIsLocal() throws UnknownHostException {
        assertTrue(Utils.isLocalAddress(new NetworkIdentifier(new InetSocketAddress("192.168.0.1", 7080))));
        assertTrue(Utils.isLocalAddress(new NetworkIdentifier(new InetSocketAddress("10.168.0.1", 7080))));
        assertFalse(Utils.isLocalAddress(new NetworkIdentifier(new InetSocketAddress("184.168.0.0", 7081))));
    }

    /*
    @Test
    public void testBlocksRange() {
        assertEquals(Pair.make(0l, 100l), Utils.range(new PieceBlock(0, 0), 100l));
        assertEquals(Pair.make(Constants.BLOCK_SIZE, Constants.BLOCK_SIZE*2), Utils.range( new PieceBlock(0, 1), 10000000l));
        assertEquals(Pair.make(Constants.BLOCK_SIZE*2, Constants.BLOCK_SIZE*3), Utils.range( new PieceBlock(0, 2), 10000000l));
        assertEquals(Pair.make(Constants.PIECE_SIZE + Constants.BLOCK_SIZE*2, Constants.PIECE_SIZE + Constants.BLOCK_SIZE*3), Utils.range( new PieceBlock(1, 2), 100000000l));
        assertEquals(Pair.make(Constants.PIECE_SIZE*5 + Constants.BLOCK_SIZE*3, Constants.PIECE_SIZE*5 + Constants.BLOCK_SIZE*3 + 100500), Utils.range( new PieceBlock(5, 3), Constants.PIECE_SIZE*5 + Constants.BLOCK_SIZE*3 + 100500));
    }
    */

    @Test
    public void testIdDetector() throws UnknownHostException {
        assertTrue(Utils.isLowId(0));
        assertFalse(Utils.isLowId(Utils.networkByteOrderToIp(InetAddress.getByName("213.127.10.1").getAddress())));
        assertTrue(Utils.isLowId(Utils.networkByteOrderToIp(InetAddress.getByName("0.0.10.1").getAddress())));
    }
}
