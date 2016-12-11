package org.dkf.jed2k.protocol.kad.test;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.kad.Kad2SearchRes;
import org.junit.Assume;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import static junit.framework.Assert.*;

/**
 * Created by inkpot on 10.12.2016.
 */
public class Kad2ResTest {

    @Test
    public void testKad2SearchResTest() throws IOException, JED2KException {
        Assume.assumeTrue(!System.getProperty("java.runtime.name").toLowerCase().startsWith("android"));
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("kad2_search_res_0.dat").getFile());
        assertTrue(file != null);
        RandomAccessFile reader = new RandomAccessFile(file, "r");
        FileChannel inChannel = reader.getChannel();
        long fileSize = inChannel.size();
        ByteBuffer buffer = ByteBuffer.allocate((int) fileSize);
        inChannel.read(buffer);
        buffer.flip();
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        inChannel.close();
        reader.close();
        assertTrue(buffer.hasRemaining());
        Kad2SearchRes res = new Kad2SearchRes();
        res.get(buffer);
        assertFalse(buffer.hasRemaining());
        assertEquals(2, res.getResults().size());
    }

    @Test
    public void testKad2SearchResTestLarge() throws IOException, JED2KException {
        Assume.assumeTrue(!System.getProperty("java.runtime.name").toLowerCase().startsWith("android"));
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("kad2_search_res_2.dat").getFile());
        assertTrue(file != null);
        RandomAccessFile reader = new RandomAccessFile(file, "r");
        FileChannel inChannel = reader.getChannel();
        long fileSize = inChannel.size();
        ByteBuffer buffer = ByteBuffer.allocate((int) fileSize);
        inChannel.read(buffer);
        buffer.flip();
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        inChannel.close();
        reader.close();
        assertTrue(buffer.hasRemaining());
        Kad2SearchRes res = new Kad2SearchRes();
        res.get(buffer);
        assertFalse(buffer.hasRemaining());
        assertEquals(8, res.getResults().size());
    }
}
