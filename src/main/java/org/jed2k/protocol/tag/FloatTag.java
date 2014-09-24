package org.jed2k.protocol.tag;

import org.jed2k.protocol.Buffer;

public class FloatTag extends Tag {
    private float value;
    
    private FloatTag(byte type, byte id, String name, float value) {
        super(type, id, name);
        this.value = value;
    }

    @Override
    public Buffer get(Buffer src) {
        value = src.getFloat();
        return src;
    }
    
    @Override
    public Buffer put(Buffer dst) {
        return super.put(dst).put(value);
    }
    
    static public FloatTag valueOf(byte id, String name, float value){
        return new FloatTag(Tag.TAGTYPE_FLOAT32, id, name, value);
    }
}