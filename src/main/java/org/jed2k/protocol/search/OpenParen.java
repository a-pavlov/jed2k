package org.jed2k.protocol.search;

import java.nio.ByteBuffer;
import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.Serializable;

public class OpenParen implements Serializable {

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        assert(false);
        return null;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        assert(false);
        return null;
    }

    @Override
    public int bytesCount() {
        assert(false);
        return 0;
    }
    
    @Override
    public String toString() {
        return "(";
    }
    
}