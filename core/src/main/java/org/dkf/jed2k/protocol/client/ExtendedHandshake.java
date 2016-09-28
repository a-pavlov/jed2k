package org.dkf.jed2k.protocol.client;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.*;
import org.dkf.jed2k.protocol.tag.Tag;

import java.nio.ByteBuffer;

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
