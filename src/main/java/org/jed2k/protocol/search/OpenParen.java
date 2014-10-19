package org.jed2k.protocol.search;

import java.nio.ByteBuffer;

import org.jed2k.protocol.ProtocolException;
import org.jed2k.protocol.Serializable;

public class OpenParen implements Serializable {

    @Override
    public ByteBuffer get(ByteBuffer src) throws ProtocolException {
        assert(false);
        return null;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws ProtocolException {
        assert(false);
        return null;
    }

    @Override
    public int size() {
        assert(false);
        return 0;
    }
    
    @Override
    public String toString() {
        return "(";
    }
    
}