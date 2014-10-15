package org.jed2k.protocol;

import java.nio.ByteBuffer;

public interface Serializable {
    public ByteBuffer get(ByteBuffer src) throws ProtocolException;
    public ByteBuffer put(ByteBuffer dst) throws ProtocolException;
    public int size();    
}