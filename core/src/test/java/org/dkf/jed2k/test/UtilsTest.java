package org.dkf.jed2k.test;

import org.dkf.jed2k.Constants;
import org.dkf.jed2k.Pair;
import org.dkf.jed2k.Utils;
import org.dkf.jed2k.protocol.NetworkIdentifier;
import org.junit.Test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;

import static junit.framework.Assert.*;
import static org.dkf.jed2k.Utils.int2Address;


public class UtilsTest {

    @Test
    public void testIpAddressConversion() throws UnknownHostException {
        assertEquals(InetAddress.getByName("0.0.0.22"), int2Address(0x16000000));
        assertEquals(InetAddress.getByName("0.0.0.16"), int2Address(0x10000000));
        assertEquals(InetAddress.getByName("127.0.0.1"), int2Address(0x0100007f));
        assertEquals(InetAddress.getByName("196.127.10.1"), int2Address(0x010a7fc4));
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
        assertFalse(Utils.isLowId(Utils.packToNetworkByteOrder(InetAddress.getByName("213.127.10.1").getAddress())));
        assertTrue(Utils.isLowId(Utils.packToNetworkByteOrder(InetAddress.getByName("1.0.0.0").getAddress())));
        assertTrue(Utils.isLowId(Utils.packToNetworkByteOrder(InetAddress.getByName("0.0.0.0").getAddress())));
    }
}
