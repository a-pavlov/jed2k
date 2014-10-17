package org.jed2k.protocol.search;

import java.nio.ByteBuffer;

import org.jed2k.protocol.ProtocolException;

import static org.jed2k.Utils.sizeof;

public class BooleanEntry extends SearchEntry {

    private final Operator value;
    
    BooleanEntry(Operator value) {
        this.value = value;
    }
    
    @Override
    public ByteBuffer get(ByteBuffer src) throws ProtocolException {
        assert(false);
        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws ProtocolException {
        dst.put(SearchRequest.SEARCH_TYPE_BOOL);
        assert(value == Operator.OPER_AND || value == Operator.OPER_OR || value == Operator.OPER_NOT);
        dst.put(value.value);
        return dst;
    }

    @Override
    public int size() {
        return sizeof(value.value) + sizeof(SearchRequest.SEARCH_TYPE_BOOL);
    }

    @Override
    public Operator getOperator() {
        return value;        
    }

    @Override
    public boolean isOperator() {
        return true;
    }
}
