package org.jed2k.protocol.server;

import org.jed2k.protocol.Serializable;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import static org.jed2k.protocol.Unsigned.uint8;
import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.ContainerHolder;
import org.jed2k.protocol.Dispatchable;
import org.jed2k.protocol.Dispatcher;
import org.jed2k.protocol.Hash;
import org.jed2k.protocol.NetworkIdentifier;
import org.jed2k.protocol.UInt8;


public class FoundFileSources implements Serializable, Dispatchable {
    public Hash hash = new Hash();
    public ContainerHolder<UInt8, NetworkIdentifier> sources = new ContainerHolder<UInt8, NetworkIdentifier>(uint8(), new LinkedList<NetworkIdentifier>(), NetworkIdentifier.class);
    
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
