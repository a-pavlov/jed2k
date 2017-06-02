package org.dkf.jed2k.disk.test;

import org.dkf.jed2k.disk.DesktopFileHandler;
import org.dkf.jed2k.disk.FileHandler;
import org.dkf.jed2k.exception.JED2KException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Created by apavlov on 01.06.17.
 */
public class FileHandlerTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void trivialFileHandlerTestUsageAfterClose() throws IOException, JED2KException {
        FileHandler fh = new DesktopFileHandler(folder.newFile("test.dat"));
        FileChannel fc = fh.getWriteChannel();
        ByteBuffer buff = ByteBuffer.allocate(10);
        buff.putInt(1).putInt(2).putShort((short)2);
        buff.flip();
        assertTrue(buff.hasRemaining());
        fc.write(buff);
        assertFalse(buff.hasRemaining());
        fh.close();
        assertFalse(fc.isOpen());
        fc = fh.getWriteChannel();
        buff.flip();
        assertTrue(buff.hasRemaining());
        fc.write(buff);
        assertFalse(buff.hasRemaining());

        FileChannel read = fh.getReadChannel();
        assertTrue(read.isOpen());
        buff.flip();
        read.read(buff);
        buff.flip();
        assertEquals(1, buff.getInt());
        assertEquals(2, buff.getInt());
        assertEquals((short)2, buff.getShort());
        assertTrue(fc.isOpen());
        assertTrue(read.isOpen());
        fh.close();
        assertFalse(fc.isOpen());
        assertFalse(read.isOpen());
    }
}
