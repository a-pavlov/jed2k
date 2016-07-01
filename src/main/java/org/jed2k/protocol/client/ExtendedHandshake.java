package org.jed2k.protocol.client;

import java.nio.ByteBuffer;
import java.util.LinkedList;

import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.Container;
import org.jed2k.protocol.Serializable;
import org.jed2k.protocol.UInt32;
import org.jed2k.protocol.UInt8;
import org.jed2k.protocol.Unsigned;
import org.jed2k.protocol.tag.Tag;

public class ExtendedHandshake implements Serializable {
    public static byte EMULE_PROTOCOL = 0x01;
    
    public final UInt8 version = Unsigned.uint8();
    public final UInt8 protocolVersion = Unsigned.uint8(EMULE_PROTOCOL);
    public final Container<UInt32, Tag> properties = Container.makeInt(Tag.class);
    
    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return properties.get(protocolVersion.get(version.get(src)));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return properties.put(protocolVersion.put(version.put(dst)));
    }

    @Override
    public int bytesCount() {
        return version.bytesCount() + protocolVersion.bytesCount() + properties.bytesCount();
    }
}
