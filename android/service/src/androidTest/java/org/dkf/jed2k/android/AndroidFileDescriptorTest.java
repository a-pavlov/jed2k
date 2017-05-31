package org.dkf.jed2k.android;

import android.os.ParcelFileDescriptor;
import org.dkf.jed2k.disk.DesktopFileHandler;
import org.dkf.jed2k.disk.FileHandler;
import org.dkf.jed2k.exception.JED2KException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static android.os.ParcelFileDescriptor.MODE_READ_WRITE;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Created by apavlov on 01.06.17.
 */
public class AndroidFileDescriptorTest {



    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void trivialFileHandlerTestUsageAfterClose() throws IOException, JED2KException {
        File f = folder.newFile("android.dat");
        ParcelFileDescriptor pfd = ParcelFileDescriptor.open(f, MODE_READ_WRITE);
        FileHandler fh = new AndroidFileHandler(f, null, pfd);
        FileChannel fc = fh.getWriteChannel();
        ByteBuffer buff = ByteBuffer.allocate(10);
        buff.putInt(1).putInt(2).putShort((short)2);
        buff.flip();
        assertTrue(buff.hasRemaining());
        fc.write(buff);
        assertFalse(buff.hasRemaining());

        fc = fh.getWriteChannel();
        buff.flip();
        assertTrue(buff.hasRemaining());
        fc.write(buff);
        assertFalse(buff.hasRemaining());

        FileChannel read = fh.getReadChannel();
        read.position(0);
        assertEquals(0, read.position());
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
