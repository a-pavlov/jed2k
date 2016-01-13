package org.jed2k;

import org.jed2k.data.PieceBlock;

public class SerialPolicy implements Policy {
    private int piecesCount;
    
    public SerialPolicy(int pieces) {
        piecesCount = pieces;
    }
    
    @Override
    public int priority(final PieceBlock pb) {
        assert(pb.piece_index < piecesCount);
        return pb.piece_index*Constants.BLOCKS_PER_PIECE + pb.piece_block;
    }
    
}