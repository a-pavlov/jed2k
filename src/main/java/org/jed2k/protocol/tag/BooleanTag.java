package org.jed2k.protocol.tag;

import org.jed2k.protocol.Buffer;
import org.jed2k.protocol.ProtocolException;

public final class BooleanTag extends Tag{
    private boolean value;
    
    private BooleanTag(byte type, byte id, String name, boolean value) {
        super(type, id, name);
        this.value = value;
    }

    @Override
    public Buffer get(Buffer src) throws ProtocolException {
        value = (src.getByte() == 0x00);    
        return src;
    }

    @Override
    public Buffer put(Buffer dst) throws ProtocolException {
        byte bval = (value)?(byte)0x01:(byte)0x00;
        super.put(dst).put(bval);
        return dst;
    }

    static public BooleanTag valueOf(byte id, String name, boolean value){
        return new BooleanTag(Tag.TAGTYPE_BOOL, id, name, value);
    }
}