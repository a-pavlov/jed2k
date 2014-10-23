package org.jed2k.protocol;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.tag.Tag;
import static org.jed2k.protocol.Unsigned.uint32;

public class ClientHelloAnswer implements Serializable {
    private Hash hash;
    private NetworkIdentifier point;
    private ContainerHolder<UInt32, Tag> properties;
    private NetworkIdentifier serverPoint;
    
    public ClientHelloAnswer() {
        hash = new Hash();
        point = new NetworkIdentifier();
        properties = new ContainerHolder<UInt32, Tag>(uint32(), new ArrayList<Tag>(), Tag.class);
    }
    
    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return serverPoint.get(properties.get(point.get(hash.get(src))));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return serverPoint.put(properties.put(point.put(hash.put(dst))));
    }

    @Override
    public int size() {
        return hash.size() + point.size() + properties.size() + serverPoint.size();
    }    
}
