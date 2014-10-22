package org.jed2k.protocol;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import static org.jed2k.protocol.Unsigned.uint8;
import static org.jed2k.protocol.Unsigned.uint16;
import static org.jed2k.protocol.Unsigned.uint32;
import org.jed2k.exception.JED2KException;

public class ByteContainer<CS extends UNumber> implements Serializable {
    public CS size;
    public byte[] value;
    
    public ByteContainer(CS size) {
        this.size = size;
    }
    
    public ByteContainer(CS size, byte[] value) {
        this.size = size;
        this.value = value;
    }
    
    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        size.get(src);
        value = new byte[size.intValue()];
        return src.get(value);
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        assert(value != null);
        assert(value.length != 0);
        size.assign(value.length);
        return size.put(dst).put(value);
    }
    
    public String asString() throws JED2KException {
        try {
            if (value != null)  return new String(value, "UTF-8");
            return null;
        } catch(UnsupportedEncodingException e) {
            throw new JED2KException(e);
        }        
    }
    
    public static<CS extends UNumber> ByteContainer<UInt8> fromString8(String value) throws JED2KException {
        try {           
            byte[] content = value.getBytes("UTF-8");
            return new ByteContainer<UInt8>(uint8(), content);
        } catch(UnsupportedEncodingException e) {
            throw new JED2KException(e);
        }
    }
    
    public static<CS extends UNumber> ByteContainer<UInt16> fromString16(String value) throws JED2KException {
        try {           
            byte[] content = value.getBytes("UTF-8");
            return new ByteContainer<UInt16>(uint16(), content);
        } catch(UnsupportedEncodingException e) {
            throw new JED2KException(e);
        }
    }
    
    public static<CS extends UNumber> ByteContainer<UInt32> fromString32(String value) throws JED2KException {
        try {           
            byte[] content = value.getBytes("UTF-8");
            return new ByteContainer<UInt32>(uint32(), content);
        } catch(UnsupportedEncodingException e) {
            throw new JED2KException(e);
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
        return size.size() + value.length;        
    }
    
}