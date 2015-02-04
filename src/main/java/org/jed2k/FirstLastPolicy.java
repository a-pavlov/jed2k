package org.jed2k;

public class FirstLastPolicy implements Policy {
    private int piecesCount;
    
    public FirstLastPolicy(int pieces) {
        piecesCount = pieces;
    }
    
    @Override
    public int priority(final PieceBlock pb) {
        int offset = pb.left + 1;   // default priority as usual plus pass last piece
        
        if (pb.left == 0) {
            offset = 0; // first piece has highest priority 
        } else if (pb.left == (piecesCount - 1)) {
            offset = 1; // last piece has highest - 1 priority
        }
        
        return offset*Constants.BLOCKS_PER_PIECE + pb.right;
    }
}
