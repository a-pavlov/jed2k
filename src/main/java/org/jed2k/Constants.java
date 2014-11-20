package org.jed2k;

public class Constants {
    public static final long PIECE_SIZE = 9728000l;
    public static final long BLOCK_SIZE = 190*1024l;    // 190kb = PIECE_SIZE/50
    public static final int BLOCKS_PER_PIECE = (int)(PIECE_SIZE/BLOCK_SIZE); // 50
    public static final long HIGHEST_LOWID_ED2K = 16777216l;
}
