package org.jed2k.protocol.tag;

import org.jed2k.protocol.Buffer;
import org.jed2k.protocol.ByteContainer;
import org.jed2k.protocol.ProtocolException;
import org.jed2k.protocol.UInt32;
import static org.jed2k.protocol.Unsigned.uint32;

public final class ArrayTag extends Tag {
    private ByteContainer<UInt32>   value = new ByteContainer<UInt32>(uint32());
    public ArrayTag(byte type, byte id, String name, byte[] value) {
        super(type, id, name);
        this.value.value = value;
        // TODO Auto-generated constructor stub
    }

    @Override
    public Buffer get(Buffer src) throws ProtocolException {
        return value.get(src);
    }
    
    @Override
    public Buffer put(Buffer dst) throws ProtocolException {
        return value.put(dst);
    }
    
    public static Tag valueOf(byte id, String name, byte[] value) {
        return new ArrayTag(Tag.TAGTYPE_BLOB, id, name, value);
    }
    
}