package org.jed2k.disk;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.Callable;

public class Writer implements Callable<WriteResult> {
    FileChannel fch;
    ByteBuffer  buffer;
    long offset;
    
    public Writer(FileChannel f, ByteBuffer b, long off) {
        fch = f;
        buffer = b;
        offset = off;
    }
    
    @Override
    public WriteResult call() throws Exception {
        try {
            fch.position(offset);
            while(buffer.hasRemaining()) fch.write(buffer);
        } catch(Exception e) {
            return new WriteResult(buffer, e);
        }
        return new WriteResult(buffer, null);
    }
}
