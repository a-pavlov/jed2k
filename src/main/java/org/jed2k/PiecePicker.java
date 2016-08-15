package org.jed2k;

import java.util.*;

import org.jed2k.data.PieceBlock;

public class PiecePicker extends BlocksEnumerator {

    private enum PieceState {
        NONE((byte)0),
        DOWNLOADING((byte)1),
        HAVE((byte)2);
        byte value;
        PieceState(byte b) {
            value = b;
        }
    }

    private class PiecePos {
        public int peersCount = 0;
        public int full = 0;
        public int priority = 0;
        public int indx = 0;
    }

    private byte pieceStatus[];
    private LinkedList<DownloadingPiece> downloadingPieces = new LinkedList<DownloadingPiece>();

    public PiecePicker(int pieceCount, int blocksInLastPiece) {
        super(pieceCount, blocksInLastPiece);
    	assert(pieceCount > 0);
        pieceStatus = new byte[pieceCount];
        Arrays.fill(pieceStatus, (byte)PieceState.NONE.value);
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
     * mark block as finished
     */
    public boolean markAsFinished(PieceBlock b) {
        assert(b.pieceBlock < blocksInPiece(b.pieceIndex));
        DownloadingPiece dp = getDownloadingPiece(b.pieceIndex);
        if (dp != null) {
            dp.finishBlock(b.pieceBlock);
            // found actual piece in downloading state
            if (dp.finishedCount() == dp.getBlocksCount()) {
                downloadingPieces.remove(dp);
                pieceStatus[b.pieceIndex] = PieceState.HAVE.value;
            }

            return true;
        }

        // piece already finished
    	return false;
    }


    public boolean markAsDownloading(PieceBlock b) {
        assert(b.pieceBlock < blocksInPiece(b.pieceIndex));
        DownloadingPiece dp = getDownloadingPiece(b.pieceIndex);

        if (dp != null) {
            dp.requestBlock(b.pieceBlock);
            return true;
        }

        return false;
    }

    public boolean markAsWriting(PieceBlock b) {
        assert(b.pieceBlock < blocksInPiece(b.pieceIndex));
        DownloadingPiece dp = getDownloadingPiece(b.pieceIndex);
        if (dp != null) {
            dp.writeBlock(b.pieceBlock);
            return true;
        }

        return false;
    }

    public void abortDownload(PieceBlock b) {
        DownloadingPiece dp = getDownloadingPiece(b.pieceIndex);
        if (dp != null) {
            dp.abortDownloading(b.pieceBlock);
        }
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

            if (pieceStatus[current] == PieceState.NONE.value) {
                downloadingPieces.add(new DownloadingPiece(current, blocksInPiece(current)));
                pieceStatus[current] = PieceState.DOWNLOADING.value;
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

    /**
     * return makes piece available for downloading again
     * @param piece index of piece
     */
    public final void restorePiece(int piece) {
        assert(piece < pieceStatus.length); // correct piece index
        assert(getDownloadingPiece(piece) == null); // must not be in download queue
        assert(pieceStatus[piece] != PieceState.NONE.value); //? TODO - it depends on pieces downloading algorithm
        pieceStatus[piece] = PieceState.NONE.value;
    }

    /**
     *
     * @return pieces count we already have
     */
    public final int numHave() {
        int res = 0;
        for(byte b: pieceStatus) {
            if (b == PieceState.HAVE.value) ++res;
        }

        return res;
    }

    /**
     *
     * @return total pieces
     */
    public final int numPieces() {
        return pieceStatus.length;
    }

    /**
     * currently pieces in downloading state
     * @return
     */
    public final int numDowloadingPieces() { return downloadingPieces.size(); }


    /**
     * mark piece as "we have" - piece flushed to disk and hash value verified
     * @param pieceIndex index of piece
     */
    public void weHave(int pieceIndex) {
        assert(pieceIndex < pieceStatus.length);
        pieceStatus[pieceIndex] = PieceState.HAVE.value;
    }

    public boolean havePiece(int pieceIndex) {
        assert(pieceIndex < pieceStatus.length);
        return pieceStatus[pieceIndex] == PieceState.HAVE.value;
    }

    /**
     * check network stage for this piece is completed
     * @param pieceIndex index of piece
     * @return true if piece in "we have" state or downloading completed - all blocks are downloaded
     */
    boolean isPieceFinished(int pieceIndex) {
        assert(pieceIndex < pieceStatus.length);
        if (pieceStatus[pieceIndex] == PieceState.NONE.value) return false;
        if (pieceStatus[pieceIndex] == PieceState.HAVE.value) return true;
        DownloadingPiece dp = getDownloadingPiece(pieceIndex);
        assert(dp != null);
        return dp.getBlocksCount() == (dp.finishedCount() + dp.writingCount());
    }

    /**
     * add piece to downloading list(if not exists) and mark block as finished
     * @param b block information
     */
    public void weHaveBlock(PieceBlock b) {
        DownloadingPiece p = getDownloadingPiece(b.pieceIndex);

        if (p == null) {
            p = new DownloadingPiece(b.pieceIndex, blocksInPiece(b.pieceIndex));
            downloadingPieces.addLast(p);
            pieceStatus[b.pieceIndex] = PieceState.DOWNLOADING.value;
        }

        assert(p != null);
        p.finishBlock(b.pieceBlock);
    }
}
