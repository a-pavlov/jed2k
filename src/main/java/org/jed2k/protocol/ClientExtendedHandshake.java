package org.jed2k.protocol;

import java.nio.ByteBuffer;
import java.util.LinkedList;

import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.tag.Tag;

public class ClientExtendedHandshake implements Serializable {
    
    public final UInt16 version = Unsigned.uint16();
    public final ContainerHolder<UInt16, Tag> properties = ContainerHolder.make16(new LinkedList<Tag>(), Tag.class);
    
    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return properties.get(version.get(src));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return properties.put(version.put(dst));
    }

    @Override
    public int bytesCount() {
        return version.bytesCount() + properties.bytesCount();
    }
}
