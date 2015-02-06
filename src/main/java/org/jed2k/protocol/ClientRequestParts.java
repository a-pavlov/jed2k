package org.jed2k.protocol;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.jed2k.exception.JED2KException;

public abstract class ClientRequestParts<SizeType extends Serializable> implements Serializable {
    
    public static int PARTS_COUNT = 3;
    public final Hash hash  = new Hash();
    public ArrayList<SizeType> beginOffset = new ArrayList<SizeType>(PARTS_COUNT);
    public ArrayList<SizeType> endOffset = new ArrayList<SizeType>(PARTS_COUNT);
    
    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        hash.get(src);
        for(Serializable s: beginOffset) s.get(src);
        for(Serializable s: endOffset) s.get(src);
        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        hash.put(dst);
        for(Serializable s: beginOffset) s.put(dst);
        for(Serializable s: endOffset) s.put(dst);
        return dst;
    }
}
