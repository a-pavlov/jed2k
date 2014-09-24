package org.jed2k.protocol.tag;

import org.jed2k.protocol.Buffer;
import org.jed2k.protocol.UInt16;
import org.jed2k.protocol.UInt32;
import org.jed2k.protocol.UInt8;
import org.jed2k.protocol.UNumber;

public class TypedTag<T extends UNumber> extends Tag{
    private final T value;
    
    private TypedTag(byte type, byte id, String name, T value) {
        super(type, id, name);
        this.value = value; 
    }

    @Override
    public Buffer get(Buffer src) {
        return value.get(src);
    }

    @Override
    public Buffer put(Buffer src) {
        return value.put(super.put(src));
    }

    public static <T extends UNumber> Tag valueOf(byte id, String name, T value){
        byte type = Tag.TAGTYPE_UNDEFINED;
        if (value instanceof UInt8) {
            type = Tag.TAGTYPE_UINT8;
        } else if ( value instanceof UInt16) {
            type = Tag.TAGTYPE_UINT16;
        } else if ( value instanceof UInt32) {
            type = Tag.TAGTYPE_UINT32;
        } else {
            
        }
        
        return new TypedTag<T>(type, id, name, value);
    }    
}