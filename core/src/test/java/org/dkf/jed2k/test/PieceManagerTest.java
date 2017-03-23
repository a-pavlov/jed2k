package org.dkf.jed2k.test;

import org.dkf.jed2k.Constants;
import org.dkf.jed2k.DesktopFileHandler;
import org.dkf.jed2k.PieceManager;
import org.dkf.jed2k.data.PieceBlock;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.pool.BufferPool;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;

import static junit.framework.Assert.assertEquals;

/**
 * Created by ap197_000 on 23.08.2016.
 */
public class PieceManagerTest {

    BufferPool pool = new BufferPool(100);

    @Rule
    public TemporaryFolder folder= new TemporaryFolder();


    @Before
    public void setUp() {

    }

    byte getBlockContent(final PieceBlock b) {
        return (byte)(b.pieceIndex* Constants.BLOCKS_PER_PIECE + b.pieceBlock);
    }

    ByteBuffer getBuffer(final PieceBlock b, int dataSize) {
        ByteBuffer buffer = pool.allocate();
        buffer.limit(dataSize);
        byte data = getBlockContent(b);
        while(buffer.hasRemaining()) {
            buffer.put(data);
        }

        buffer.flip();
        return buffer;
    }

    @Test
    public void selfTest() {
        assertEquals((byte)Constants.BLOCKS_PER_PIECE, getBlockContent(new PieceBlock(1, 0)));
        assertEquals((byte)51, getBlockContent(new PieceBlock(1, 1)));
        assertEquals((byte)152, getBlockContent(new PieceBlock(3, 2)));
        assertEquals((byte)10, getBlockContent(new PieceBlock(0, 10)));
    }

    @Test
    public void testNormalRestore() throws IOException, JED2KException {
        File f = folder.newFile("pm1.dat");
        PieceManager pm = new PieceManager(new DesktopFileHandler(f), 3, 3);
        LinkedList<ByteBuffer> res0 = pm.writeBlock(new PieceBlock(0, 0), getBuffer(new PieceBlock(0, 0), Constants.BLOCK_SIZE_INT));
        assertEquals(1, res0.size());
        LinkedList<ByteBuffer> res1 = pm.writeBlock(new PieceBlock(1, 0), getBuffer(new PieceBlock(1, 0), Constants.BLOCK_SIZE_INT));

        // restore piece manager using external block
        PieceManager rpm = new PieceManager(new DesktopFileHandler(f), 3, 3);
        ByteBuffer b = ByteBuffer.allocate(Constants.BLOCK_SIZE_INT);
        LinkedList<ByteBuffer> rres = rpm.restoreBlock(new PieceBlock(0, 0), b, Constants.BLOCK_SIZE_INT*Constants.BLOCKS_PER_PIECE*2+123);
        assertEquals(1, rres.size());

        // prepare buffers for piece 0.0
        res0.get(0).clear();
        res0.get(0).limit(Constants.BLOCK_SIZE_INT);

        rres.get(0).clear();
        rres.get(0).limit(Constants.BLOCK_SIZE_INT);
        while(res0.get(0).hasRemaining()) {
            assertEquals(res0.get(0).get(), rres.get(0).get());
        }

        rres = rpm.restoreBlock(new PieceBlock(1, 0), b, Constants.BLOCK_SIZE_INT*Constants.BLOCKS_PER_PIECE*2+123);
        assertEquals(1, rres.size());

        res1.get(0).clear();
        res1.get(0).limit(Constants.BLOCK_SIZE_INT);
        rres.get(0).clear();
        rres.get(0).limit(Constants.BLOCK_SIZE_INT);
        while(res1.get(0).hasRemaining()) {
            assertEquals(res1.get(0).get(), rres.get(0).get());
        }
    }
}
