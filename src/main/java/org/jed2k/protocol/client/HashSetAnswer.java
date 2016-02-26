package org.jed2k.protocol.client;

import java.nio.ByteBuffer;
import java.util.LinkedList;

import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.ContainerHolder;
import org.jed2k.protocol.Dispatchable;
import org.jed2k.protocol.Dispatcher;
import org.jed2k.protocol.Hash;
import org.jed2k.protocol.Serializable;
import org.jed2k.protocol.UInt16;

public class HashSetAnswer implements Serializable, Dispatchable {
    public final Hash hash = new Hash();
    public final ContainerHolder<UInt16, Hash> parts = ContainerHolder.make16(new LinkedList<Hash>(), Hash.class);
    
    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return parts.get(hash.get(src));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return parts.put(hash.put(dst));
    }

    @Override
    public int bytesCount() {
        return hash.bytesCount() + parts.bytesCount();
    }

    @Override
    public void dispatch(Dispatcher dispatcher) throws JED2KException {
        dispatcher.onClientHashSetAnswer(this);        
    }
}
