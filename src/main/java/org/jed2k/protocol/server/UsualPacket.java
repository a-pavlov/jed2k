package org.jed2k.protocol.server;

import org.jed2k.protocol.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.Container;
import org.jed2k.protocol.Hash;
import org.jed2k.protocol.NetworkIdentifier;
import org.jed2k.protocol.UInt32;
import org.jed2k.protocol.tag.Tag;

import static org.jed2k.protocol.Unsigned.uint32;

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