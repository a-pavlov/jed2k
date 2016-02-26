package org.jed2k.protocol.client;

import java.nio.ByteBuffer;

import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.ByteContainer;
import org.jed2k.protocol.Dispatchable;
import org.jed2k.protocol.Dispatcher;
import org.jed2k.protocol.Hash;
import org.jed2k.protocol.Serializable;
import org.jed2k.protocol.UInt16;
import org.jed2k.protocol.Unsigned;

public class FileAnswer implements Serializable, Dispatchable {
    public Hash hash = new Hash();
    public ByteContainer<UInt16> name = new ByteContainer<UInt16>(Unsigned.uint16());

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return name.get(hash.get(src));
    }
    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return name.put(hash.put(dst));
    }
    @Override
    public int bytesCount() {
        return hash.bytesCount() + name.bytesCount();
    }
    
    @Override
    public void dispatch(Dispatcher dispatcher) throws JED2KException {
       dispatcher.onClientFileAnswer(this);       
    }
}
