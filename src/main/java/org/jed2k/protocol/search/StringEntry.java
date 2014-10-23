package org.jed2k.protocol.search;

import java.nio.ByteBuffer;

import org.jed2k.protocol.ByteContainer;
import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.Serializable;

import org.jed2k.protocol.UInt16;

import static org.jed2k.Utils.sizeof;

public class StringEntry implements Serializable {
    private ByteContainer<UInt16> value;
    private ByteContainer<UInt16> tag;
    
    
    public StringEntry(ByteContainer<UInt16> value, ByteContainer<UInt16> tag) {
        this.value = value;
        this.tag = tag;
    }
    
    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        assert(false);
        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        if (tag != null) {
            dst.put(SearchRequest.SEARCH_TYPE_STR_TAG);            
        } else {
            dst.put(SearchRequest.SEARCH_TYPE_STR);
        }
        
        value.put(dst);
        
        if (tag != null) {
            tag.put(dst);
        }
        
        return dst;
    }

    @Override
    public int bytesCount() {
        return sizeof(SearchRequest.SEARCH_TYPE_STR) + ((tag != null)?tag.bytesCount():0) + value.bytesCount();
    }
    
    @Override
    public String toString() {        
        try {
            return value.asString();
        } catch (JED2KException e) {
            return " Exception ";            
        }
    }
}
