package org.jed2k;

import java.util.ArrayList;
import java.util.Collection;

import org.jed2k.data.PieceBlock;
import org.jed2k.data.PieceInfo;

public class PiecePicker {

    private class PiecePos {
        public int peersCount = 0;
        public int full = 0;
        public int priority = 0;
        public int indx = 0;
    }

    ArrayList<PieceInfo>	pieces;
    private ArrayList<PiecePos> piece_map;
    private int blocksInLastPiece = 0;
    private int roundRobin;
    
    /**
     *	all pieces before this index are finished 
     */
    private int finishedPiecesBorder;

    public void init(int blocksInLastPiece, int totalPieces) {
        for(int i = 0; i != totalPieces; ++i) {
            piece_map.add(new PiecePos());
        }

        this.blocksInLastPiece = blocksInLastPiece;
    }

    public int blocksInPiece(int pieceIndex) {
        assert(pieceIndex < piece_map.size());
        if (pieceIndex == piece_map.size()) {
            return blocksInLastPiece;
        }

        return Constants.BLOCKS_PER_PIECE;
    }
    
    public PiecePicker(int pieceCount, int blocksInLastPiece) {
    	assert(pieceCount > 0);
    	finishedPiecesBorder = 0;
    	pieces = new ArrayList<PieceInfo>(pieceCount);
    	for(int i = 0; i < pieceCount; ++i) {
    		int blocksCount = (i == (pieceCount - 1))?blocksInLastPiece:Constants.BLOCKS_PER_PIECE;
    		pieces.add(new PieceInfo(blocksCount));
    	}
        roundRobin = pieceCount - 1;
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
    public boolean markAsFinished(PieceBlock b) {
        assert(b.piece_block < blocksInPiece(b.piece_index));
    	return false;
    }


    public boolean markAsDownloading(PieceBlock b) {
        assert(b.piece_block < blocksInPiece(b.piece_index));
        return false;
    }

    public int pickPieces(Collection<PieceBlock> rq, int orderLength) {
        return 0;
    }

}
