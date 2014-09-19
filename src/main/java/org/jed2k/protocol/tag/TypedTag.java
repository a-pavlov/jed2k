package org.jed2k.protocol.tag;

import org.jed2k.protocol.Buffer;
import org.jed2k.protocol.UNumber;

public class TypedTag<T extends UNumber> extends Tag{
    private T value;
        
    @Override
    public Buffer get(Buffer src) {        
        return value.get(src);
    }
    
    @Override
    public Buffer put(Buffer src) {
        return value.put(super.put(src));
    }
    
}