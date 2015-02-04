package org.jed2k;

public class SerialPolicy implements Policy {
    private int piecesCount;
    
    public SerialPolicy(int pieces) {
        piecesCount = pieces;
    }
    
    @Override
    public int priority(final PieceBlock pb) {
        assert(pb.piece() < piecesCount);
        return pb.left*Constants.BLOCKS_PER_PIECE + pb.right;
    }
    
}