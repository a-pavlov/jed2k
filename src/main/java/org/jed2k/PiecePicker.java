package org.jed2k;

import java.util.ArrayList;

import org.jed2k.data.PieceBlock;
import org.jed2k.data.PieceInfo;

public class PiecePicker {
    ArrayList<PieceInfo>	pieces;
    
    /**
     *	all pieces before this index are finished 
     */
    private int finishedPiecesBorder;
    
    public PiecePicker(int pieceCount, int blocksInLastPiece) {
    	assert(pieceCount > 0);
    	finishedPiecesBorder = 0;
    	pieces = new ArrayList<PieceInfo>(pieceCount);
    	for(int i = 0; i < pieceCount; ++i) {
    		int blocksCount = (i == (pieceCount - 1))?blocksInLastPiece:Constants.BLOCKS_PER_PIECE;
    		pieces.add(new PieceInfo(blocksCount));
    	}
    }
    
    /**
     * request block here
     * returns next interested block or null if no new blocks are available for requesting
     * 
     */    
    public PieceBlock requestBlock() {        
    	for(int i = finishedPiecesBorder; i != pieces.size(); ++i) {
    	    int block = pieces.get(i).requestBlock();
    	    if (block != -1) {
    	        return new PieceBlock(i, block);
    	    }
    	}
    	
    	return null;
    }
    
    /**
     * return piece to picker
     * it might happen when calculated piece hash doesn't match provided
     * @param index - index of piece 
     */
    public void returnPiece(int index) {
    	
    }
    
    /**
     * mark block as finished and update border
     */
    public void finishBlock(PieceBlock block) {
    	
    }
}
