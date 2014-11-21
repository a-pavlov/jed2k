package org.jed2k.protocol;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.tag.Tag;

public class ClientHelloAnswer implements Serializable, Dispatchable {
    public final Hash hash = new Hash();
    public final NetworkIdentifier point = new NetworkIdentifier();
    public final ContainerHolder<UInt32, Tag> properties = ContainerHolder.make32(new ArrayList<Tag>(), Tag.class);
    public final NetworkIdentifier serverPoint = new NetworkIdentifier();
       
    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return serverPoint.get(properties.get(point.get(hash.get(src))));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return serverPoint.put(properties.put(point.put(hash.put(dst))));
    }

    @Override
    public int bytesCount() {
        return hash.bytesCount() + point.bytesCount() + properties.bytesCount() + serverPoint.bytesCount();
    }

    @Override
    public void dispatch(Dispatcher dispatcher) throws JED2KException {
        dispatcher.onClientHelloAnswer(this);
    }
    
    @Override
    public String toString() {
        return hash.toString() + " " + point + " " + properties;
    }    
}
