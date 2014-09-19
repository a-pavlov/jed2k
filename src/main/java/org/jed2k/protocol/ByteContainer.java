package org.jed2k.protocol;

import java.io.UnsupportedEncodingException;

public class ByteContainer<CS extends UNumber> implements Serializable{
    public CS size;
    public byte[] value;
    
    public ByteContainer(CS size){
        this.size = size;
    }
    
    @Override
    public Buffer get(Buffer src) {
        size.get(src);
        value = new byte[size.intValue()];
        return src.get(value);
    }

    @Override
    public Buffer put(Buffer dst) {
        size.assign(value!=null?value.length:0);
        return dst.put(value);        
    }
    
    @Override
    public String toString(){
        try{
            if (value != null)
                return new String(value, "UTF-8");
        }
        catch(UnsupportedEncodingException ex){
            // Suppress exception
        }
        
        return new String();
    }     
}