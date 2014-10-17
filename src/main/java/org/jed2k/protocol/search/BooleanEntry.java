package org.jed2k.protocol.search;

import java.nio.ByteBuffer;

import org.jed2k.protocol.ProtocolException;
import org.jed2k.protocol.Serializable;

import static org.jed2k.Utils.sizeof;

public class BooleanEntry implements Serializable {

    private byte operator = 0;
    
    BooleanEntry(byte operator) {
        this.operator = operator;
    }
    
    @Override
    public ByteBuffer get(ByteBuffer src) throws ProtocolException {
        assert(false);
        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws ProtocolException {
        dst.put(SearchRequest.SEARCH_TYPE_BOOL);
        dst.put(operator);
        return dst;
    }

    @Override
    public int size() {
        return sizeof(operator) + sizeof(SearchRequest.SEARCH_TYPE_BOOL);
    }
}
