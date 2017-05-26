package org.dkf.jed2k.data;

import org.dkf.jed2k.Constants;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Serializable;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * PieceBlock describes block of data in piece
 * each piece contains few few blocks, from 1 to BLOCKS_PER_PIECE constant
 */
public class PieceBlock implements Comparable<PieceBlock>, Serializable {
    public int pieceIndex;
    public int pieceBlock;

    public PieceBlock() {
        pieceIndex = -1;
        pieceBlock = -1;
    }

    public PieceBlock(int p, int b) {
        assert p >= 0;
        assert b >= 0;
        pieceIndex = p;
        pieceBlock = b;
    }

    /**
     *
     * @return offset of current block in blocks
     */
    public Long blocksOffset() {
        return new Long((long) pieceIndex *Constants.BLOCKS_PER_PIECE + (long) pieceBlock);
    }

    public static PieceBlock mkBlock(final PeerRequest r) {
        return new PieceBlock(r.piece, (int)(r.start / Constants.BLOCK_SIZE));
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
    public int size(long totalSize) {
        Range r = range(totalSize);
        return (int)(r.right - r.left);
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

    @Override
    public int hashCode() {
        return pieceIndex*Constants.BLOCKS_PER_PIECE + pieceBlock;
    }

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        try {
            pieceIndex = src.getInt();
            pieceBlock = src.getInt();
        } catch(BufferUnderflowException e) {
            throw new JED2KException(ErrorCode.BUFFER_UNDERFLOW_EXCEPTION);
        } catch(Exception e) {
            throw new JED2KException(ErrorCode.BUFFER_GET_EXCEPTION);
        }
        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return dst.putInt(pieceIndex).putInt(pieceBlock);
    }

    @Override
    public int bytesCount() {
        return 4 + 4;
    }
}
