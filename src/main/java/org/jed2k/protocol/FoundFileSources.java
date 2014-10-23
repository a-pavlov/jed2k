package org.jed2k.protocol;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import static org.jed2k.protocol.Unsigned.uint8;
import org.jed2k.exception.JED2KException;


public class FoundFileSources implements Serializable {
    public Hash hash = new Hash();
    ContainerHolder<UInt8, NetworkIdentifier> sources = new ContainerHolder<UInt8, NetworkIdentifier>(uint8(), new LinkedList<NetworkIdentifier>(), NetworkIdentifier.class);
    
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
}