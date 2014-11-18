package org.jed2k;

public class PiecePicker {
    private int pieces;
    private int blocksInLastPiece;
    
    public PiecePicker(int pieces, int blocksInLastPiece) {
        this.pieces = pieces;
        this.blocksInLastPiece = blocksInLastPiece;
    }
    
    public PieceBlock requestBlock() {
        return new PieceBlock(0,0);
    }
    
    public void finalizeBlock(PieceBlock block, int status) {
        
    }
}
