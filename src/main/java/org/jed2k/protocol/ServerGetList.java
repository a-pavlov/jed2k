package org.jed2k.protocol;

import java.nio.ByteBuffer;

public class ServerGetList implements Serializable {

    @Override
    public ByteBuffer get(ByteBuffer src) throws ProtocolException {
        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws ProtocolException {
        return dst;
    }

    @Override
    public int size() {        
        return 0;
    }
    
}