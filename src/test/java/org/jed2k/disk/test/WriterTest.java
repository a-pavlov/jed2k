package org.jed2k.disk.test;

import org.jed2k.disk.WriteResult;
import org.jed2k.disk.Writer;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static junit.framework.Assert.assertEquals;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class WriterTest {

    @Test
    public void testWriteFile() throws Exception {
        String newData = "New String to write to file..." + System.currentTimeMillis();
        RandomAccessFile aFile     = new RandomAccessFile("nio-data.txt", "rw");
        FileChannel channel = aFile.getChannel();
        ByteBuffer buf = ByteBuffer.allocate(48);
        buf.clear();
        buf.put(newData.getBytes());
        buf.flip();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<WriteResult> wres = executorService.submit(new Writer(channel, buf, 100l));
        while(!wres.isDone()) {
            try {            
                wres.get(1L, TimeUnit.MILLISECONDS);
            } catch(TimeoutException e) {
                
            }
        }
        
        assertEquals(wres.get().except, null);
        executorService.shutdown();
        aFile.close();
    }
}
