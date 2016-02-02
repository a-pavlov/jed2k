package org.jed2k.disk;

import java.nio.ByteBuffer;

public class WriteResult {
    public ByteBuffer buffer;
    public Exception except;    
    
    public WriteResult(ByteBuffer b, Exception e) {
        buffer = b;
        except = e;
    }
}
