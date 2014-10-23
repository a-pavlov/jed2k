package org.jed2k.protocol;

import java.nio.ByteBuffer;
import static org.jed2k.Utils.sizeof;
import org.jed2k.exception.JED2KException;

public class GetFileSources implements Serializable {
    public Hash hash = new Hash();
    public int lowPart  = 0;
    public int hiPart   = 0;
    
    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        hash.get(src);
        int val = src.getInt();
        
        if (val == 0) {
            // we has high path
            lowPart = src.getInt();
            hiPart  = src.getInt();
        } else {
            lowPart = val;
        }
        
        return null;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        hash.put(dst);
        
        if (hiPart != 0) {
            dst.putInt(0);            
        }
        
        dst.putInt(lowPart);
        
        if (hiPart != 0) {
            dst.putInt(hiPart);
        }
        
        return dst;       
    }

    @Override
    public int bytesCount() {
        return hash.bytesCount() + sizeof(lowPart) + ((hiPart != 0)?sizeof(hiPart):0);      
    }
    
}