package org.jed2k.protocol.tag;

import org.jed2k.protocol.Buffer;
import org.jed2k.protocol.ProtocolException;
import org.jed2k.protocol.UInt16;

import static org.jed2k.protocol.Unsigned.uint16;

/*
 *  Stub class - only for skip byte array space
 */
public final class BoolArrayTag extends Tag {    
    private final UInt16 length = uint16();
    private byte value[] = null;
    
    public BoolArrayTag(byte type, byte id, String name) {
        super(type, id, name);        
        // TODO Auto-generated constructor stub
    }

    @Override
    public Buffer get(Buffer src) throws ProtocolException {
        length.get(src);
        value = new byte[length.intValue() / 8];
        return src.get(value);        
    }    
}