package org.jed2k.protocol;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class ByteContainer<CS extends UNumber> implements Serializable {
    public CS size;
    public byte[] value;
    
    public ByteContainer(CS size){
        this.size = size;
    }
    
    @Override
    public ByteBuffer get(ByteBuffer src) throws ProtocolException {
        size.get(src);
        value = new byte[size.intValue()];
        return src.get(value);
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws ProtocolException {
        size.assign(value!=null?value.length:0);
        return dst.put(value);
    }
    
    public String asString() throws ProtocolException {
        try {
            if (value != null)  return new String(value, "UTF-8");
            return null;
        } catch(UnsupportedEncodingException e) {
            throw new ProtocolException(e);
        }        
    }
    
    @Override
    public String toString() {
        try{
            if (value != null)
                return new String(value, "UTF-8");
        }
        catch(UnsupportedEncodingException ex){
            // Suppress exception
        }
        
        return new String();
    }

    
    @Override
    public int size() {
        return size.size() + size.intValue();        
    }
    
}