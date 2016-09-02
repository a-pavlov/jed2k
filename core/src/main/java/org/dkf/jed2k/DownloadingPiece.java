package org.dkf.jed2k;

import java.util.Arrays;
import java.util.Iterator;

public class DownloadingPiece implements Iterable<DownloadingPiece.BlockState> {

    public enum BlockState {
        STATE_NONE((byte)0),
        STATE_REQUESTED((byte)1),
        STATE_WRITING((byte)2),
        STATE_FINISHED((byte)3);
        byte value;

        BlockState(byte b) {
            value = b;
        }
    }

    private class Block {
        public BlockState   state;
    }

    public int pieceIndex;
    private int blocksCount;
    public BlockState[] blockState;
    // TODO - for future usage cached values
    short requested;
    short writing;
    short finished;

    public DownloadingPiece(int pieceIndex, int blocksCount) {
        assert(pieceIndex >= 0);
        assert(blocksCount > 0);
        this.pieceIndex = pieceIndex;
        this.blocksCount = blocksCount;
        blockState = new BlockState[blocksCount];
        Arrays.fill(blockState, BlockState.STATE_NONE);
    }

    public int finishedCount() {
        int res = 0;
        for(int i = 0; i < blocksCount; ++i) {
            if (blockState[i] == BlockState.STATE_FINISHED) res++;
        }
        return res;
    }

    public int downloadingCount() {
        int res = 0;
        for(int i = 0; i < blocksCount; ++i) {
            if (blockState[i] == BlockState.STATE_REQUESTED) res++;
        }
        return res;
    }

    public int writingCount() {
        int res = 0;
        for(int i = 0; i < blocksCount; ++i)
            if (blockState[i] == BlockState.STATE_WRITING) res++;
        return res;
    }

    public final int downloadedCount() {
        int res = 0;
        for(int i = 0; i < blocksCount; ++i) {
            if (isDownloaded(i)) res++;
        }

        return res;
    }

    public int getBlocksCount() {
        return blocksCount;
    }

    public void finishBlock(int blockIndex) {
        assert(blockIndex < blocksCount);
        blockState[blockIndex] = BlockState.STATE_FINISHED;
    }

    public void requestBlock(int blockIndex) {
        assert(blockIndex < blocksCount);
        blockState[blockIndex] = BlockState.STATE_REQUESTED;
    }

    public void writeBlock(int blockIndex) {
        assert(blockIndex < blocksCount);
        blockState[blockIndex] = BlockState.STATE_WRITING;
    }

    boolean isDownloaded(int blockIndex) {
        assert(blockIndex < blocksCount);
        return blockState[blockIndex] == BlockState.STATE_FINISHED ||
                blockState[blockIndex] == BlockState.STATE_WRITING;
    }

    boolean isFinished(int blockIndex) {
        assert(blockIndex < blocksCount);
        return blockState[blockIndex] == BlockState.STATE_FINISHED;
    }

    public void abortDownloading(int blockIndex) {
        blockState[blockIndex] = BlockState.STATE_NONE;
    }

    @Override
    public Iterator<BlockState> iterator() {
        Iterator<BlockState> it = new Iterator<BlockState>() {
            private int currentIndex = 0;

            @Override
            public boolean hasNext() {
                assert(blockState != null);
                return currentIndex < blockState.length;
            }

            @Override
            public BlockState next() {
                return blockState[currentIndex++];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };

        return it;
    }
}
