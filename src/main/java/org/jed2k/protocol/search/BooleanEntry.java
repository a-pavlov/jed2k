package org.jed2k.protocol.search;

import java.nio.ByteBuffer;

import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.Serializable;

import static org.jed2k.Utils.sizeof;

public class BooleanEntry implements Serializable {

    public enum Operator {
        OPER_AND(0x01),
        OPER_OR(0x02),
        OPER_NOT(0x03);
        
        public final byte value;
        
        Operator(int value) {
            this.value = (byte)value;
        }
    }
    
    private final Operator value;
    
    BooleanEntry(Operator value) {
        this.value = value;
    }
    
    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        assert(false);
        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
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
    public String toString() {
        if (value == Operator.OPER_AND) return "AND";
        if (value == Operator.OPER_OR) return "OR";
        if (value == Operator.OPER_NOT) return "NOT";
        assert(false);
        return "UNKNOWN"; 
    }
    
    public Operator operator() {
        return value;
    }
}
