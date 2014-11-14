package org.jed2k.protocol;

import java.nio.ByteBuffer;

import org.jed2k.exception.JED2KException;

public class ClientFileStatusAnswer implements Serializable, Dispatchable {
    public final Hash hash = new Hash();
    public final BitField bitfield = new BitField();
    
    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return bitfield.get(hash.get(src));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return bitfield.put(hash.put(dst));
    }

    @Override
    public int bytesCount() {
        return hash.bytesCount() + bitfield.bytesCount();
    }

    @Override
    public void dispatch(Dispatcher dispatcher) throws JED2KException {
       dispatcher.onClientFileStatusAnswer(this);       
    }
    
}
