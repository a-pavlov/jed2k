package org.jed2k;

/**
 * Created by inkpot on 19.07.2016.
 * trivial blocks counter for pieces queue, for each except last returns Constants.BLOCKS_PER_PIECE
 * and blocksInLastPiece for last piece
 */
public class BlocksEnumerator {
    private int pieceCount;
    private int blocksInLastPiece;

    public BlocksEnumerator(int pieceCount, int blocksInLastPiece) {
        assert(pieceCount > 0);
        assert(blocksInLastPiece > 0);
        this.pieceCount = pieceCount;
        this.blocksInLastPiece = blocksInLastPiece;
    }


    public int blocksInPiece(int pieceIndex) {
        assert(pieceIndex < pieceCount);
        if (pieceIndex == pieceCount - 1) {
            return blocksInLastPiece;
        }

        return Constants.BLOCKS_PER_PIECE;
    }
}
