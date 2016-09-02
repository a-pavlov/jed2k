package org.dkf.jed2k.test;

import org.dkf.jed2k.BlockManager;
import org.dkf.jed2k.Constants;
import org.dkf.jed2k.hash.MD4;
import org.dkf.jed2k.protocol.Hash;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.TreeSet;
import java.util.logging.Logger;

import static junit.framework.Assert.*;

/**
 * Created by inkpot on 17.07.2016.
 */
public class BlockManagerTest {

    private static Logger log = Logger.getLogger(BlockManagerTest.class.getName());

    ByteBuffer buffer;
    Hash pieceHash;

    @Before
    public void setUp() {
        buffer = ByteBuffer.allocate((int)Constants.PIECE_SIZE);
        Random rnd = new Random();
        for(int i = 0; i < (int)Constants.PIECE_SIZE / 4; ++i) {
            buffer.putInt(rnd.nextInt());
        }

        int pos = buffer.position();
        assert(buffer.position() >= Constants.PIECE_SIZE - 4);
        buffer.flip();
        byte[] data = new byte[(int)Constants.PIECE_SIZE];
        buffer.get(data);
        buffer.rewind();
        assert(buffer.remaining() == (int)Constants.PIECE_SIZE);
        MD4 hasher = new MD4();
        hasher.update(data);
        pieceHash = Hash.fromBytes(hasher.digest());
        assert(pieceHash != null);
    }

    @Test
    public void testSequentialOrder() {
        BlockManager bm = new BlockManager(0, Constants.BLOCKS_PER_PIECE);
        for(int i = 0; i < Constants.BLOCKS_PER_PIECE; ++i) {
            buffer.position(i*(int)Constants.BLOCK_SIZE);
            ByteBuffer localBuffer = buffer.slice();
            localBuffer.limit((int)Constants.BLOCK_SIZE);
            //log.info("Bf: " + localBuffer);
            LinkedList<ByteBuffer> res = bm.registerBlock(i, localBuffer);
            assertFalse(res.isEmpty());
            assertEquals(1, res.size());
            assertEquals(localBuffer, res.getFirst());
        }

        assertTrue(bm.pieceHash() != null);
        assertEquals(pieceHash, bm.pieceHash());
    }

    @Test
    public void testRandomOrder() {
        BlockManager bm = new BlockManager(0, Constants.BLOCKS_PER_PIECE);
        TreeSet<ByteBuffer> src = new TreeSet<ByteBuffer>();    // source buffers to managers
        TreeSet<ByteBuffer> dst = new TreeSet<ByteBuffer>();    // result buffers after registration
        byte[] usedBlocks = new byte[Constants.BLOCKS_PER_PIECE];
        Arrays.copyOf(usedBlocks, 0);
        Random rnd = new Random();

        for(int i = 0; i < Constants.BLOCKS_PER_PIECE; ++i) {
            int pos = rnd.nextInt(Constants.BLOCKS_PER_PIECE);

            // choose free block in round robin fashion
            if (usedBlocks[pos] != 0) {
                int roundRobin = pos;
                for(int j = 0; j < usedBlocks.length; ++j) {
                    if (usedBlocks[roundRobin] != 0) {
                        roundRobin++;
                        if (roundRobin == usedBlocks.length) roundRobin = 0;
                    } else {
                        pos = roundRobin;
                        break;
                    }
                }
            }

            assertEquals(0, usedBlocks[pos]);
            usedBlocks[pos] = 1;

            buffer.position(pos*(int)Constants.BLOCK_SIZE);
            ByteBuffer localBuffer = buffer.slice();
            localBuffer.limit((int)Constants.BLOCK_SIZE);
            assertTrue(src.add(localBuffer));
            LinkedList<ByteBuffer> res = bm.registerBlock(pos, localBuffer);
            if (res != null) {
                for(ByteBuffer bb: res) {
                    assertTrue(bb != null);
                    bb.rewind();
                    assertTrue(dst.add(bb));
                }
            }
        }

        // validate final hash
        assertTrue(bm.pieceHash() != null);
        assertEquals(pieceHash, bm.pieceHash());

        // validate all source buffers were returned
        src.retainAll(dst);
        assertEquals(src.size(), Constants.BLOCKS_PER_PIECE);
    }
}
