package org.dkf.jed2k.test;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dkf.jed2k.Checker;
import org.dkf.jed2k.Constants;
import org.dkf.jed2k.Pair;
import org.dkf.jed2k.Utils;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.Hash;
import org.junit.Test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static junit.framework.Assert.*;
import static org.dkf.jed2k.Utils.*;


public class UtilsTest {

    @Test
    public void testIpAddressConversion() throws UnknownHostException {
        assertEquals(InetAddress.getByName("0.0.0.22"), int2Address(0x16000000));
        assertEquals(InetAddress.getByName("0.0.0.16"), int2Address(0x10000000));
        assertEquals(InetAddress.getByName("127.0.0.1"), int2Address(0x0100007f));
        assertEquals(InetAddress.getByName("196.127.10.1"), int2Address(0x010a7fc4));
    }

    @Test
    public void testIpAddressToString() {
        assertEquals("0.0.0.22", ip2String(0x16000000));
        assertEquals("0.0.0.16", ip2String(0x10000000));
        assertEquals("127.0.0.1", ip2String(0x0100007f));
        assertEquals("196.127.10.1", ip2String(0x010a7fc4));
    }

    @Test
    public void testString2Ip() throws JED2KException {
        assertEquals(0x16000000, string2Ip("0.0.0.22"));
        assertEquals(0x10000000, string2Ip("0.0.0.16"));
        assertEquals(0x0100007f, string2Ip("127.0.0.1"));
        assertEquals(0x010a7fc4, string2Ip("196.127.10.1"));
    }

    @Test
    public void testTwoDirectionsTransformation() throws JED2KException {
        assertEquals("192.168.0.33", ip2String(string2Ip("192.168.0.33")));
        assertEquals("255.255.255.255", ip2String(string2Ip("255.255.255.255")));
        assertEquals("88.122.32.45", ip2String(string2Ip("88.122.32.45")));
    }

    @Test(expected = JED2KException.class)
    public void testIllegalArguments() throws JED2KException {
        string2Ip("192.678.33.0");
    }

    @Test(expected = JED2KException.class)
    public void testIllegalArguments2() throws JED2KException {
        string2Ip("192.678..0");
    }

    @Test(expected = JED2KException.class)
    public void testIllegalArguments3() throws JED2KException {
        string2Ip("192.678.0");
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
        assertTrue(Utils.isLocalAddress(new Endpoint(new InetSocketAddress("192.168.0.1", 7080))));
        assertTrue(Utils.isLocalAddress(new Endpoint(new InetSocketAddress("10.168.0.1", 7080))));
        assertFalse(Utils.isLocalAddress(new Endpoint(new InetSocketAddress("184.168.0.0", 7081))));
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

    @Test
    public void testLinksGeneration() {
        assertEquals("ed2k://|file|some_file|100|31D6CFE0D16AE931B73C59D7E0C089C0|/", Utils.formatLink("some_file", 100l, Hash.TERMINAL));
    }

    @Test
    public void testBitSat() {
        assertTrue(Utils.isBit(1, 1));
        assertFalse(Utils.isBit(2, 1));
        assertTrue(Utils.isBit(3, 1));
        int value = 0;
        value |= 64;
        value |= 32;
        assertTrue(Utils.isBit(value, 32));
        assertTrue(Utils.isBit(value, 64));
        assertFalse(Utils.isBit(value, 16));
        assertFalse(Utils.isBit(value, 1));
    }

    @Data
    @EqualsAndHashCode
    private static class Stub {

        public Stub(int f, int s) {
            first = f;
            second = s;
        }

        private int first;
        private int second;
    }

    private static class StubCheck implements Checker<Stub> {
        private int firstTarget;

        public StubCheck(int f) {
            firstTarget = f;
        }

        @Override
        public boolean check(Stub stub) {
            return stub.getFirst() == firstTarget;
        }
    }

    @Data
    private static class Node {
        private boolean pinged;
        public Node(boolean pinged) {
            this.pinged = pinged;
        }
    }

    @Test
    public void testIndexOf() {
        List<Stub> data = new ArrayList<>();
        data.add(new Stub(1,2));
        data.add(new Stub(0, 4));
        data.add(new Stub(5,5));

        assertEquals(-1, Utils.indexOf(data, new StubCheck(4)));
        assertEquals(0, Utils.indexOf(data, new StubCheck(1)));
        assertEquals(1, Utils.indexOf(data, new StubCheck(0)));
    }

    @Test
    public void testIndexOf2() {
        List<Node> nodes = new LinkedList<>();
        nodes.add(new Node(true));
        nodes.add(new Node(true));
        nodes.add(new Node(false));
        nodes.add(new Node(false));
        nodes.add(new Node(true));
        nodes.add(new Node(false));
        assertEquals(2, Utils.indexOf(nodes, new Checker<Node>() {
            @Override
            public boolean check(Node node) {
                return !node.isPinged();
            }
        }));
    }
}

