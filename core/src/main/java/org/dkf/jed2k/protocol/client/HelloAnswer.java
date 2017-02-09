package org.dkf.jed2k.protocol.client;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.*;
import org.dkf.jed2k.protocol.tag.Tag;

import java.nio.ByteBuffer;

public class HelloAnswer implements Serializable, Dispatchable {
    public final Hash hash = new Hash();
    public final Endpoint point = new Endpoint();
    public final Container<UInt32, Tag> properties = Container.makeInt(Tag.class);
    public final Endpoint serverPoint = new Endpoint();

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
