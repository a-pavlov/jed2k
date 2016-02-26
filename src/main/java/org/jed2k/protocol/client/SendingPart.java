package org.jed2k.protocol.client;

import java.nio.ByteBuffer;

import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.Hash;
import org.jed2k.protocol.Serializable;
import org.jed2k.protocol.UNumber;

public abstract class SendingPart<SizeType extends UNumber> implements Serializable {
    public final Hash hash = new Hash();
    public SizeType beginOffset = null;
    public SizeType endOffset = null;

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
        return hash.bytesCount() + beginOffset.bytesCount()*2;                
    }
    
    public abstract int payloadSize();
}
