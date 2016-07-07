package org.jed2k;

import org.jed2k.data.PieceBlock;
import org.jed2k.protocol.Hash;
import java.util.ArrayList;
import java.util.Arrays;

public class DownloadingPiece {
    private enum BlockState {
        STATE_NONE((byte)0),
        STATE_REQUESTED((byte)1),
        STATE_DOWNLOADING((byte)2),
        STATE_FINISHED((byte)3);
        byte value;

        BlockState(byte b) {
            value = b;
        }
    }

    private class Block {
        public BlockState   state;
    }

    private int pieceIndex;
    private int blocksCount;
    private byte[] blockState;
    
    public DownloadingPiece(int pieceIndex, int c) {
        assert(pieceIndex >= 0);
        assert(c > 0);
        this.pieceIndex = pieceIndex;
        blocksCount = c;
        blockState = new byte[blocksCount];
        Arrays.fill(blockState, BlockState.STATE_NONE.value);
    }

    public int finishedCount() {
        int res = 0;
        for(int i = 0; i < blocksCount; ++i) {
            if (blockState[i] == BlockState.STATE_FINISHED.value) res++;
        }
        return res;
    }

    public int requestedCount() {
        int res = 0;
        for(int i = 0; i < blocksCount; ++i) {
            if (blockState[i] == BlockState.STATE_REQUESTED.value) res++;
        }
        return res;
    }
}
