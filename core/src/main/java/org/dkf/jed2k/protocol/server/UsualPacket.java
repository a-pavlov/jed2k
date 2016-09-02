package org.dkf.jed2k.protocol.server;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.*;
import org.dkf.jed2k.protocol.tag.Tag;

import java.nio.ByteBuffer;

public class UsualPacket implements Serializable {
    public Hash    hash = new Hash();
    public NetworkIdentifier   point = new NetworkIdentifier();
    public Container<UInt32, Tag> properties = Container.makeInt(Tag.class);

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return properties.get(point.get(hash.get(src)));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return properties.put(point.put(hash.put(dst)));
    }

    @Override
    public int bytesCount() {
        return hash.bytesCount() + point.bytesCount() + properties.bytesCount();
    }

    @Override
    public String toString() {
        return hash.toString() + " " + point.toString() + " " + properties.toString();
    }
}