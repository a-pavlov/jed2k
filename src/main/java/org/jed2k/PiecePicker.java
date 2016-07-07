package org.jed2k;

import java.util.*;

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
    private byte pieceStatus[];
    private LinkedList<DownloadingPiece> downloadingPieces;
    
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
        pieceStatus = new byte[pieceCount];
        Arrays.fill(pieceStatus, (byte)0);
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

    /**
     * choose next piece and add it to download queue
     * @return true if new piece in download queue
     */
    public boolean chooseNextPiece() {
        // start from last piece
        int roundRobin = pieces.size() - 1;
        for(int i = 0; i < pieces.size(); ++i) {
            if (roundRobin == pieces.size()) roundRobin = 0;
            int current = roundRobin;

            if (pieceStatus[current] == 0) {
                downloadingPieces.add(new DownloadingPiece(current, blocksInPiece(current)));
                pieceStatus[current] = (byte)1;
                return true;
            }

            ++roundRobin;
        }

        return false;
    }

    /**
     *
     * @param rq - request queue
     * @param orderLength - prefer blocks count for request
     */
    public void pickPieces(Collection<PieceBlock> rq, int orderLength) {
        Iterator<DownloadingPiece> itr = downloadingPieces.iterator();
        while(itr.hasNext()) {
            DownloadingPiece dp = itr.next();
            // add block to order
        }

        if (rq.size() < orderLength && chooseNextPiece()) {
            pickPieces(rq, orderLength);
        }
    }

}
