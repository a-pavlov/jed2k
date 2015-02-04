package org.jed2k.test;

import org.jed2k.Constants;
import org.jed2k.Policy;
import org.jed2k.FirstLastPolicy;
import org.jed2k.SerialPolicy;
import org.jed2k.PieceBlock;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class PolicyTest {
    
    @Test
    public void testSerialPolicy() {
        Policy policy = new SerialPolicy(4);
        PieceBlock pb = new PieceBlock(1, 45);
        assertEquals(1, pb.left.intValue());
        assertEquals(45, pb.right.intValue());
        
        assertEquals(0, policy.priority(new PieceBlock(0, 0)));
        assertEquals(Constants.BLOCKS_PER_PIECE-1,      policy.priority(new PieceBlock(0, Constants.BLOCKS_PER_PIECE-1)));
        assertEquals(Constants.BLOCKS_PER_PIECE*2 - 1,  policy.priority(new PieceBlock(1, Constants.BLOCKS_PER_PIECE-1)));
        assertEquals(Constants.BLOCKS_PER_PIECE*3,      policy.priority(new PieceBlock(3, 0)));
        assertEquals(Constants.BLOCKS_PER_PIECE*4-1,    policy.priority(new PieceBlock(3, Constants.BLOCKS_PER_PIECE-1)));
    }
    
    @Test
    public void testFirstLastPolicy() {
        Policy policy = new FirstLastPolicy(5);
        
        // check first piece
        assertEquals(0, policy.priority(new PieceBlock(0, 0)));
        assertEquals(Constants.BLOCKS_PER_PIECE-1,      policy.priority(new PieceBlock(0, Constants.BLOCKS_PER_PIECE-1)));
        
        // check last piece
        assertEquals(Constants.BLOCKS_PER_PIECE, policy.priority(new PieceBlock(4, 0)));
        assertEquals(Constants.BLOCKS_PER_PIECE*2-1, policy.priority(new PieceBlock(4, Constants.BLOCKS_PER_PIECE-1)));
        
        // check medium pieces
        assertEquals(Constants.BLOCKS_PER_PIECE*3 - 1,  policy.priority(new PieceBlock(1, Constants.BLOCKS_PER_PIECE-1)));
        assertEquals(Constants.BLOCKS_PER_PIECE*4,      policy.priority(new PieceBlock(3, 0)));
        assertEquals(Constants.BLOCKS_PER_PIECE*5-1,    policy.priority(new PieceBlock(3, Constants.BLOCKS_PER_PIECE-1)));
    }
}