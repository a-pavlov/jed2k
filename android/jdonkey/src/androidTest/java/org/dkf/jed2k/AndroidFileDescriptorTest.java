package org.dkf.jed2k;

import android.os.ParcelFileDescriptor;
import org.dkf.jed2k.disk.FileHandler;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jmule.AndroidFileHandler;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static android.os.ParcelFileDescriptor.MODE_READ_WRITE;
import static junit.framework.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;

/**
 * Created by apavlov on 01.06.17.
 */
@MediumTest
@RunWith(AndroidJUnit4.class)
public class AndroidFileDescriptorTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void trivialFileHandlerTestUsage() throws IOException, JED2KException {
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

    @Test
    public void trivialFileHandlerTestUsageAfterCloseChannels() throws IOException, JED2KException {
        File f = folder.newFile("android2.dat");
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
        fh.closeChannels();
        assertFalse(fc.isOpen());

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
        assertFalse(fc.isOpen());
        assertTrue(read.isOpen());
        fh.close();
        assertFalse(fc.isOpen());
        assertFalse(read.isOpen());
    }
}
