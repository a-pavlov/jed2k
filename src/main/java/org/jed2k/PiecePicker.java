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

    private int blocksInLastPiece = 0;
    private byte pieceStatus[];
    private LinkedList<DownloadingPiece> downloadingPieces = new LinkedList<DownloadingPiece>();
    
    /**
     *	all pieces before this index are finished 
     */
    private int finishedPiecesBorder;

    public int blocksInPiece(int pieceIndex) {
        assert(pieceIndex < pieceStatus.length);
        if (pieceIndex == pieceStatus.length - 1) {
            return blocksInLastPiece;
        }

        return Constants.BLOCKS_PER_PIECE;
    }
    
    public PiecePicker(int pieceCount, int blocksInLastPiece) {
    	assert(pieceCount > 0);
        this.blocksInLastPiece = blocksInLastPiece;
    	finishedPiecesBorder = 0;
        pieceStatus = new byte[pieceCount];
        Arrays.fill(pieceStatus, (byte)0);
    }

    /**
     * return piece to picker
     * it might happen when calculated piece hash doesn't match provided
     * @param index - index of piece 
     */
    public DownloadingPiece getDownloadingPiece(int index) {
    	assert(index >=0);
        Iterator<DownloadingPiece> itr = downloadingPieces.iterator();
        while(itr.hasNext()) {
            DownloadingPiece dp = itr.next();
            if (dp.pieceIndex == index) return dp;
        }

        return null;
    }
    
    /**
     * mark block as finished and update border
     */
    public boolean markAsFinished(PieceBlock b) {
        assert(b.piece_block < blocksInPiece(b.piece_index));
        DownloadingPiece dp = getDownloadingPiece(b.piece_index);
        if (dp != null) {
            // found actual piece in downloading state
            downloadingPieces.remove(dp);
            return true;
        }

        // piece already finished
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
        int roundRobin = pieceStatus.length - 1;
        for(int i = 0; i < pieceStatus.length; ++i) {
            if (roundRobin == pieceStatus.length) roundRobin = 0;
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
            for(int i = 0; i < dp.blockState.length; ++i) {
                if (dp.blockState[i] == DownloadingPiece.BlockState.STATE_NONE.value) {
                    rq.add(new PieceBlock(dp.pieceIndex, i));
                    dp.blockState[i] = DownloadingPiece.BlockState.STATE_REQUESTED.value;
                    if (rq.size() == orderLength) return;
                }
            }
        }

        if (rq.size() < orderLength && chooseNextPiece()) {
            pickPieces(rq, orderLength);
        }
    }

}
