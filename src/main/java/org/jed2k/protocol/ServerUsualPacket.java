package org.jed2k.protocol;

import java.util.ArrayList;
import org.jed2k.protocol.tag.Tag;
import static org.jed2k.protocol.Unsigned.uint32;

public class ServerUsualPacket implements Serializable {
    private Hash    hash = new Hash();
    private NetworkIdentifier   point = new NetworkIdentifier();
    private ContainerHolder<UInt32, Tag> properties = new ContainerHolder<UInt32, Tag>(uint32(), new ArrayList<Tag>(), Tag.class);
    
    @Override
    public Buffer get(Buffer src) throws ProtocolException {
        return properties.get(point.get(hash.get(src)));
    }

    @Override
    public Buffer put(Buffer dst) throws ProtocolException {
        return properties.put(point.put(hash.put(dst)));
    }
}