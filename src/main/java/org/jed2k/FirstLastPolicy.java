package org.jed2k;

import org.jed2k.data.PieceBlock;

public class FirstLastPolicy implements Policy {
    private int piecesCount;
    
    public FirstLastPolicy(int pieces) {
        piecesCount = pieces;
    }
    
    @Override
    public int priority(final PieceBlock pb) {
        int offset = pb.piece_index + 1;   // default priority as usual plus pass last piece
        
        if (pb.piece_index == 0) {
            offset = 0; // first piece has highest priority 
        } else if (pb.piece_index == (piecesCount - 1)) {
            offset = 1; // last piece has highest - 1 priority
        }
        
        return offset*Constants.BLOCKS_PER_PIECE + pb.piece_block;
    }
}
