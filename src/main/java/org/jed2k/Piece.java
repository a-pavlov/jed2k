package org.jed2k;

import org.jed2k.data.PieceBlock;
import org.jed2k.protocol.Hash;
import java.util.ArrayList;
import java.util.Arrays;

public class Piece {
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

    private int blocksCount;
    private byte[] blockState;
    
    public Piece(int c) {
        assert(c > 0);
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
