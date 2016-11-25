package org.dkf.jed2k.protocol.kad.test;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.kad.KadNodesDat;
import org.junit.Assume;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import static junit.framework.Assert.assertTrue;

/**
 * Created by inkpot on 15.11.2016.
 */
public class KadNodesDatTest {

    @Test
    public void testNodesDatFile() throws IOException, JED2KException {
        Assume.assumeTrue(!System.getProperty("java.runtime.name").toLowerCase().startsWith("android"));
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("nodes.dat").getFile());
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
        KadNodesDat nodes = new KadNodesDat();
        nodes.get(buffer);
        assertTrue(!nodes.getContacts().isEmpty());
    }
}
