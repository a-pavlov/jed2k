package org.jed2k.protocol;

import java.nio.ByteBuffer;

import org.jed2k.exception.JED2KException;

public abstract class ClientSendingPart<SizeType extends UNumber> implements Serializable {
    public final Hash hash = new Hash();
    public final SizeType beginOffset = null;
    public final SizeType endOffset = null;
    public byte[] data;
        
    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return endOffset.get(beginOffset.get(hash.get(src)));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return endOffset.put(beginOffset.put(hash.put(dst)));
    }
    
    @Override
    public int bytesCount() {
        return hash.bytesCount() + beginOffset.bytesCount()*2 + dataSize();                
    }
    
    public abstract int dataSize();
}
