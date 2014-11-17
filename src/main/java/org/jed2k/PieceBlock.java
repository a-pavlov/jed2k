package org.jed2k;

public class PieceBlock extends Pair<Integer, Integer> {
    
    public PieceBlock(int piece, int block) {
        super(piece, block);
    }
    
    public long piece() {
        return left;
    }
    
    public long block() {
        return right;
    }

    @Override
    public String toString() {
        return "[" + left + ":" + right + "]"; 
    }
}
