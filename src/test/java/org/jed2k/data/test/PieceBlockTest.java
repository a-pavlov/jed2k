package org.jed2k.data.test;

import static junit.framework.Assert.assertEquals;

import org.jed2k.Constants;
import org.jed2k.data.PieceBlock;
import org.jed2k.data.Range;
import org.junit.Test;

/**
 * Created by inkpot on 27.06.2016.
 */
public class PieceBlockTest {
    @Test
    public void testTrivialBlockCreation() {
        assertEquals(new PieceBlock(0, 0), PieceBlock.make(10L));
        assertEquals(new PieceBlock(0, 0), PieceBlock.make(0L));
        assertEquals(new PieceBlock(0, 0), PieceBlock.make(1040L));
        assertEquals(new PieceBlock(0, 1), PieceBlock.make(Constants.BLOCK_SIZE));
        assertEquals(new PieceBlock(0, 3), PieceBlock.make(Constants.BLOCK_SIZE*3));
        assertEquals(new PieceBlock(1, 0), PieceBlock.make(Constants.BLOCK_SIZE*Constants.BLOCKS_PER_PIECE));
        assertEquals(new PieceBlock(1, 1), PieceBlock.make(Constants.BLOCK_SIZE*Constants.BLOCKS_PER_PIECE + Constants.BLOCK_SIZE));
        assertEquals(new PieceBlock(1, 1), PieceBlock.make(Constants.BLOCK_SIZE*Constants.BLOCKS_PER_PIECE + Constants.BLOCK_SIZE + 1234L));
    }

    @Test
    public void testBlocksOffset() {
        PieceBlock pb = PieceBlock.make(Constants.BLOCK_SIZE*Constants.BLOCKS_PER_PIECE + Constants.BLOCK_SIZE + 1255L);
        assertEquals(new PieceBlock(1,1), pb);
        assertEquals(Constants.BLOCKS_PER_PIECE + 1, pb.blocksOffset().longValue());
    }

    @Test
    public void testRanges() {
        PieceBlock pb = PieceBlock.make(Constants.BLOCK_SIZE*Constants.BLOCKS_PER_PIECE + Constants.BLOCK_SIZE + 1255L);
        assertEquals(new PieceBlock(1,1), pb);
        // file has enoug size and full current block
        assertEquals(new Range(Constants.BLOCK_SIZE*Constants.BLOCKS_PER_PIECE
                + Constants.BLOCK_SIZE, Constants.BLOCK_SIZE*Constants.BLOCKS_PER_PIECE + Constants.BLOCK_SIZE*2)
                , pb.range(Constants.BLOCK_SIZE*Constants.BLOCKS_PER_PIECE + Constants.BLOCK_SIZE*3));
        // partial block
        assertEquals(new Range(Constants.BLOCK_SIZE*Constants.BLOCKS_PER_PIECE
                        + Constants.BLOCK_SIZE, Constants.BLOCK_SIZE*Constants.BLOCKS_PER_PIECE + Constants.BLOCK_SIZE + 1235L)
                , pb.range(Constants.BLOCK_SIZE*Constants.BLOCKS_PER_PIECE + Constants.BLOCK_SIZE + 1235L));
    }
}
