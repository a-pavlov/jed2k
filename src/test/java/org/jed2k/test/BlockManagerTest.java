package org.jed2k.test;

import com.sun.org.apache.bcel.internal.classfile.ConstantInteger;
import org.jed2k.BlockManager;
import org.jed2k.Constants;
import org.jed2k.hash.MD4;
import org.jed2k.protocol.Hash;
import org.junit.Before;
import org.junit.Test;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Random;

/**
 * Created by inkpot on 17.07.2016.
 */
public class BlockManagerTest {

    ByteBuffer buffer;
    Hash pieceHash;

    @Before
    public void setUp() {
        buffer = ByteBuffer.allocate((int)Constants.PIECE_SIZE);
        Random rnd = new Random();
        for(int i = 0; i < (int)Constants.PIECE_SIZE / 4; ++i) {
            buffer.putInt(rnd.nextInt());
        }

        int pos = buffer.position();
        assert(buffer.position() >= Constants.PIECE_SIZE - 4);
        buffer.flip();
        byte[] data = new byte[(int)Constants.PIECE_SIZE];
        buffer.get(data);
        buffer.rewind();
        assert(buffer.remaining() == (int)Constants.PIECE_SIZE);
        MD4 hasher = new MD4();
        hasher.update(data);
        pieceHash = Hash.fromBytes(hasher.digest());
        assert(pieceHash != null);
    }

    @Test
    public void testSequentialOrder() {
        BlockManager bm = new BlockManager(0, Constants.BLOCKS_PER_PIECE);
        for(int i = 0; i < Constants.BLOCKS_PER_PIECE; ++i) {
            buffer.position(i*(int)Constants.BLOCK_SIZE);
            ByteBuffer localBuffer = buffer.slice();
            localBuffer.limit((int)Constants.BLOCK_SIZE);
            LinkedList<ByteBuffer> res = bm.registerBlock(i, localBuffer);
            assertFalse(res.isEmpty());
            assertEquals(1, res.size());
            assertEquals(localBuffer, res.getFirst());
        }

        assertTrue(bm.pieceHash() != null);
        assertEquals(pieceHash, bm.pieceHash());
    }
}
