package org.jed2k.data;

import org.jed2k.Constants;
import org.jed2k.Pair;

/**
 * PieceBlock describes block of data in piece
 * each piece contains few few blocks, from 1 to BLOCKS_PER_PIECE constant
 */
public class PieceBlock implements Comparable<PieceBlock> {
    public int pieceIndex;
    public int pieceBlock;

    public PieceBlock(int pi, int pb) {
        pieceIndex = pi;
        pieceBlock = pb;
    }

    /**
     *
     * @return offset of current block in blocks
     */
    public Long blocksOffset() {
        return new Long((long) pieceIndex *Constants.BLOCKS_PER_PIECE + (long) pieceBlock);
    }

    public static PieceBlock mkBlock(final PeerRequest r) {
        return new PieceBlock((int)r.piece, (int)(r.start / Constants.BLOCK_SIZE));
    }

    /**
     *
     * @param offset in file
     * @return block which covers this offset
     */
    public static PieceBlock make(long offset) {
        int piece = (int)(offset / Constants.PIECE_SIZE);
        int start = (int)(offset % Constants.PIECE_SIZE);
        return new PieceBlock(piece, (int)(start / Constants.BLOCK_SIZE));
    }

    /**
     *
     * @param size of file
     * @return range which block covers
     */
    public Range range(long size) {
        long begin = pieceIndex * Constants.PIECE_SIZE + pieceBlock * Constants.BLOCK_SIZE;
        long normalEnd = pieceIndex * Constants.PIECE_SIZE + (pieceBlock + 1) * Constants.BLOCK_SIZE;
        long end = Math.min(begin + Constants.BLOCK_SIZE, Math.min(normalEnd, size));
        assert(begin < end);
        return Range.make(begin, end);
    }

    /**
     *
     * @param totalSize full size of file
     * @return block size in bytes
     */
    public long size(long totalSize) {
        Range r = range(totalSize);
        return (r.right - r.left);
    }

    @Override
    public int compareTo(PieceBlock o) {
        return blocksOffset().compareTo(o.blocksOffset());
    }

    @Override
    public String toString() {
        return String.format("piece{%d} block{%d}", pieceIndex, pieceBlock);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof PieceBlock) {
            PieceBlock x = (PieceBlock)obj;
            return this.compareTo(x) == 0;
        }

        return false;
    }
}
