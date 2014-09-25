package org.jed2k.protocol.tag;

import java.io.UnsupportedEncodingException;

import org.jed2k.protocol.Buffer;
import org.jed2k.protocol.ProtocolException;


public class StringTag extends Tag {
    private String value;
    
    private StringTag(byte type, byte id, String name, String value) {
        super(type, id, name);
        this.value = value;
    }

    @Override
    public Buffer get(Buffer src) throws ProtocolException {
        short size = 0;
        if (type >= Tag.TAGTYPE_STR1 && type <= Tag.TAGTYPE_STR16){
            size = (short)(type - Tag.TAGTYPE_STR1 + 1);
        } else {
            size = src.getShort();
        }
        
        byte[] data = new byte[size];
        src.get(data);
        try {
            value = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ProtocolException(e);
        }
        
        return src;
    }
    
    @Override
    public Buffer put(Buffer dst) throws ProtocolException {
        try {
            byte[] data = value.getBytes("UTF-8");  
            super.put(dst);
            if (type == Tag.TAGTYPE_STRING)
                dst.put((short)data.length);            
            dst.put(data);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return dst;
    }

    static public StringTag valueOf(byte id, String name, String value){
            
        byte type = Tag.FT_UNDEFINED;
        try {
        int bytesCount = value.getBytes("UTF-8").length; 
        if (bytesCount <= 16) {
            type = (byte)(Tag.TAGTYPE_STR1 + bytesCount - 1);
        }
       
            
        } catch(UnsupportedEncodingException ex) {
            
        }
        
        return new StringTag(type, id, name, value);
    }
}