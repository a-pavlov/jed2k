package org.jed2k.protocol;

import java.nio.ByteBuffer;

import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.tag.Tag;

public class ClientExtendedHandshake implements Serializable {
    private UInt16 version;
    private ContainerHolder<UInt16, Tag> properties;
    
    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return properties.get(version.get(src));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return properties.put(version.put(dst));
    }

    @Override
    public int size() {
        return version.size() + properties.size();
    }
}
