package org.jed2k.disk.test;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;

import org.jed2k.Constants;
import org.junit.Test;

public class DiskIOManagerTest {
    
    @Test
    public void testFile() throws FileNotFoundException, IOException {        
        RandomAccessFile f = new RandomAccessFile("f", "rw");
        f.setLength(Constants.PIECE_SIZE);
        FileChannel channel = f.getChannel();
        LinkedList<ByteBuffer> windows = new LinkedList<ByteBuffer>();
        for(int i = 0; i < Constants.BLOCKS_PER_PIECE; ++i) {
            windows.add(channel.map(FileChannel.MapMode.READ_WRITE, i*Constants.BLOCK_SIZE, Constants.BLOCK_SIZE));
        }
        
        int index = 0;
        for(ByteBuffer bb: windows) {
            for(int i = 0; i < Constants.BLOCK_SIZE; ++i) {
                bb.put((byte)index);
            }
            ++index;
        }
        
        windows.clear();
        f.close();
        InputStream istream = new FileInputStream("f");
        for(int i = 0; i < Constants.PIECE_SIZE; ++i) {
            int value = i/(int)Constants.BLOCK_SIZE;
            assertEquals(value, istream.read());
        }
        
        istream.close();
    }
}
