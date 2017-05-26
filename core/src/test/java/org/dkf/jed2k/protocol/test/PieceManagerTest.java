package org.dkf.jed2k.protocol.test;

import org.dkf.jed2k.Constants;
import org.dkf.jed2k.data.PieceBlock;
import org.dkf.jed2k.disk.DesktopFileHandler;
import org.dkf.jed2k.disk.PieceManager;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.hash.MD4;
import org.dkf.jed2k.protocol.Hash;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static junit.framework.Assert.assertEquals;

/**
 * Created by inkpot on 19.07.2016.
 */
public class PieceManagerTest {

    ByteBuffer buffer;
    Hash pieceHash;

    @Before
    public void setUp() {
        buffer = ByteBuffer.allocate((int) Constants.PIECE_SIZE);
        for(int i = 0; i < (int)Constants.PIECE_SIZE; ++i) {
            buffer.put((byte)(i/Constants.BLOCKS_PER_PIECE));
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

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void testSequentialWritingBuffers() throws IOException, JED2KException {
        File tempFile = testFolder.newFile("file.txt");
        PieceManager pm = new PieceManager(new DesktopFileHandler(tempFile), 1, Constants.BLOCKS_PER_PIECE);
        for(int i = 0; i < Constants.BLOCKS_PER_PIECE; ++i) {
            buffer.position(i*(int)Constants.BLOCK_SIZE);
            ByteBuffer localBuffer = buffer.slice();
            localBuffer.limit((int)Constants.BLOCK_SIZE);
            assertEquals(Constants.BLOCK_SIZE_INT, localBuffer.remaining());
            pm.writeBlock(new PieceBlock(0, i), localBuffer);
        }
    }
}
