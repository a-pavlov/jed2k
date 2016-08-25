package org.jed2k;

import org.jed2k.hash.MD4;
import org.jed2k.protocol.Hash;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Created by inkpot on 17.07.2016.
 * this class designed to store information about dedicated piece
 * and partial hash
 */
public class BlockManager {
    private Hash pieceHash;
    private MD4 hasher = new MD4();
    private int lastHashedBlock = -1;
    private ByteBuffer[] buffers;
    private int piece;

    public BlockManager(int piece, int buffersCount) {
        this.piece = piece;
        buffers = new ByteBuffer[buffersCount];
        Arrays.fill(buffers, null);
    }

    public LinkedList<ByteBuffer> registerBlock(int blockIndex, ByteBuffer buffer) {
        assert pieceHash == null;
        assert(buffer.hasRemaining());
        assert(blockIndex < buffers.length);
        // have no holes - hash all contiguous blocks
        assert(buffers[blockIndex] == null);
        buffers[blockIndex] = buffer;
        if (lastHashedBlock + 1 == blockIndex) {
            LinkedList<ByteBuffer> res = new LinkedList<>();
            for(int i = blockIndex; i != buffers.length; ++i) {
                if (buffers[i] != null) lastHashedBlock++; else break;
                assert(lastHashedBlock == i);
                // TODO - fix this useless copy!
                byte[] b = new byte[buffers[i].remaining()];
                buffers[i].get(b);
                hasher.update(b);
                assert(buffers[i] != null);
                res.addLast(buffers[i]);
                buffers[i] = null;
            }

            return res;
        }

        return null;
    }

    public Hash pieceHash() {
        if (pieceHash == null) {
            assert(lastHashedBlock == buffers.length - 1);
            pieceHash = Hash.fromBytes(hasher.digest());
        }

        return pieceHash;
    }

    public int getPieceIndex() {
        return piece;
    }

    final int getByteBuffersCount() {
        int res = 0;
        for(ByteBuffer b: buffers) {
            if (b != null) ++res;
        }

        return res;
    }
}
