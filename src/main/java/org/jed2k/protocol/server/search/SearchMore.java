package org.jed2k.protocol.server.search;

import java.nio.ByteBuffer;
import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.Serializable;

public class SearchMore implements Serializable {

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return dst;
    }

    @Override
    public int bytesCount() {
        return 0;
    }
    
}