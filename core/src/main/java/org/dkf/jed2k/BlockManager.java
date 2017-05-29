package org.dkf.jed2k;

import org.dkf.jed2k.hash.MD4;
import org.dkf.jed2k.protocol.Hash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by inkpot on 17.07.2016.
 * this class designed to store information about dedicated piece
 * and partial getHash
 */
public class BlockManager {
    private static final Logger log = LoggerFactory.getLogger(BlockManager.class);
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

    public List<ByteBuffer> registerBlock(int blockIndex, ByteBuffer buffer) {
        log.debug("register block {} last hashed block {}", blockIndex, lastHashedBlock);
        List<ByteBuffer> freeBuffers = new LinkedList<>();
        assert pieceHash == null;
        assert(buffer.hasRemaining());
        assert(blockIndex < buffers.length);
        freeBuffers.clear();
        // have no holes - getHash all contiguous blocks
        assert(buffers[blockIndex] == null);
        buffers[blockIndex] = buffer;
        if (lastHashedBlock + 1 == blockIndex) {
            for(int i = blockIndex; i != buffers.length; ++i) {
                if (buffers[i] != null) lastHashedBlock++; else break;
                assert(lastHashedBlock == i);
                assert(buffers[i] != null);
                assert(buffers[i].hasRemaining());
                hasher.update(buffers[i]);
                assert(!buffers[i].hasRemaining());
                freeBuffers.add(buffers[i]);
                buffers[i] = null;
            }
        }

        return freeBuffers;
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

    public final int getByteBuffersCount() {
        int res = 0;
        for(ByteBuffer b: buffers) {
            if (b != null) ++res;
        }

        return res;
    }

    /**
     *
     * @return list of active buffers
     */
    public List<ByteBuffer> getBuffers() {
        List<ByteBuffer> res = new LinkedList<>();
        for(ByteBuffer b: buffers) {
            if (b != null) res.add(b);
        }

        return res;
    }
}
