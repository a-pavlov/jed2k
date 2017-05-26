package org.dkf.jed2k;

public class Constants {
    public static final long PIECE_SIZE = 9728000L;
    public static final long BLOCK_SIZE = 190* 1024L;    // 190kb = PIECE_SIZE/50
    public static final int BLOCK_SIZE_INT = (int)BLOCK_SIZE;
    public static final int BLOCKS_PER_PIECE = (int)(PIECE_SIZE/BLOCK_SIZE); // 50
    public static final long HIGHEST_LOWID_ED2K = 16777216L;
    public static final int REQUEST_QUEUE_SIZE = 3;
    public static final int PARTS_IN_REQUEST = 3;
}
