package org.jed2k.protocol;

import org.jed2k.protocol.tag.Tag;

public class SharedFileEntry implements Serializable {
    private Hash    hash;
    private NetworkIdentifier point;
    private ContainerHolder<UInt32, Tag> properties;
        
    @Override
    public Buffer get(Buffer src) throws ProtocolException {
        return properties.get(point.get(hash.get(src)));
    }

    @Override
    public Buffer put(Buffer dst) throws ProtocolException {
        return properties.put(point.put(hash.put(dst)));
    }
}