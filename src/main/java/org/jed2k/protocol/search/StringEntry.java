package org.jed2k.protocol.search;

import java.nio.ByteBuffer;

import org.jed2k.protocol.ByteContainer;
import org.jed2k.protocol.ProtocolException;
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
    public ByteBuffer get(ByteBuffer src) throws ProtocolException {
        assert(false);
        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws ProtocolException {
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
    public int size() {
        return sizeof(SearchRequest.SEARCH_TYPE_STR) + ((tag != null)?tag.size():0) + value.size();
    }
}
