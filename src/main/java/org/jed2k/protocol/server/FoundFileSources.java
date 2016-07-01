package org.jed2k.protocol.server;

import org.jed2k.protocol.*;

import java.nio.ByteBuffer;
import org.jed2k.exception.JED2KException;

public class FoundFileSources implements Serializable, Dispatchable {
    public Hash hash = new Hash();
    public Container<UInt8, NetworkIdentifier> sources = Container.makeByte(NetworkIdentifier.class);
    
    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return sources.get(hash.get(src));    
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return sources.put(hash.put(dst));
    }

    @Override
    public int bytesCount() {
        return hash.bytesCount() + sources.bytesCount();
    }
    
    
    @Override
    public String toString() {
        return hash + " = " + sources;
    }

    @Override
    public void dispatch(Dispatcher dispatcher) throws JED2KException {
        dispatcher.onFoundFileSources(this);
    }
}
