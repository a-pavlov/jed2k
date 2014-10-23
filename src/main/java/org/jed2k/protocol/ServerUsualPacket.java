package org.jed2k.protocol;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import org.jed2k.exception.JED2KException;

import org.jed2k.protocol.tag.Tag;

import static org.jed2k.protocol.Unsigned.uint32;

public class ServerUsualPacket implements Serializable {
    public Hash    hash = new Hash();
    public NetworkIdentifier   point = new NetworkIdentifier();
    public ContainerHolder<UInt32, Tag> properties = new ContainerHolder<UInt32, Tag>(uint32(), new ArrayList<Tag>(), Tag.class);
    
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
    
    public Tag get(int index) {
        assert(index < properties.bytesCount());
        return ((ArrayList<Tag>)properties.collection).get(index);
    }
    
    @Override
    public String toString() {
        return hash.toString() + " " + point.toString() + " " + properties.toString();
    }
}