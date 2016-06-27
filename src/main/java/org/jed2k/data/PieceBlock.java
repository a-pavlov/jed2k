package org.jed2k.data;

import org.jed2k.Constants;
import org.jed2k.Pair;

/**
 * PieceBlock describes block of data in piece
 * each piece contains few few blocks, from 1 to BLOCKS_PER_PIECE constant
 */
public class PieceBlock implements Comparable<PieceBlock> {
    public int piece_index;
    public int piece_block;
    
    public PieceBlock(int pi, int pb) {
        piece_index = pi;
        piece_block = pb;
    }

    /**
     *
     * @return offset of current block in blocks
     */
    public Long blocksOffset() {
        return new Long((long)piece_index*Constants.BLOCKS_PER_PIECE + (long)piece_block);
    }
    
    public static PieceBlock mk_block(final PeerRequest r) {
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
        long begin = piece_index * Constants.PIECE_SIZE + piece_block * Constants.BLOCK_SIZE;
        long normal_end = piece_index * Constants.PIECE_SIZE + (piece_block + 1) * Constants.BLOCK_SIZE;
        long end = Math.min(begin + Constants.BLOCK_SIZE, Math.min(normal_end, size));
        assert(begin < end);
        return Range.make(begin, end);
    }

    /**
     *
     * @param size of file
     * @return block size in bytes
     */
    public long Size(long size) {        
        Pair<Long, Long> r = range(size);
        return (r.right - r.left);
    }
    
    @Override
    public int compareTo(PieceBlock o) {
        return blocksOffset().compareTo(o.blocksOffset());
    }
    
    @Override
    public String toString() {
        return "[piece/block]: {" + Integer.toString(piece_index) + "/" + Integer.toString(piece_block) + "}";
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
