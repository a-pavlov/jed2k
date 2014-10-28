package org.jed2k.protocol;

import java.nio.ByteBuffer;

import org.jed2k.exception.JED2KException;

public class ClientFileStatusAnswer implements Serializable {
    public final Hash hash = new Hash();
    // bitfield!
    
    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int bytesCount() {
        // TODO Auto-generated method stub
        return 0;
    }
    
}
