package org.dkf.jed2k.protocol.kad;

import lombok.Getter;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Container;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.UInt16;
import org.dkf.jed2k.protocol.UInt8;
import org.dkf.jed2k.protocol.tag.Tag;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 15.11.2016.
 */
@Getter
public class Kad2Hello implements Serializable {
    private KadId kid = new KadId();
    private UInt16 portTcp = new UInt16();
    private UInt8 version = new UInt8();
    private Container<UInt8, Tag> info = Container.makeByte(Tag.class);

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return info.get(version.get(portTcp.get(kid.get(src))));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return info.put(version.put(portTcp.put(kid.put(dst))));
    }

    @Override
    public int bytesCount() {
        return kid.bytesCount() + portTcp.bytesCount() + version.bytesCount() + info.bytesCount();
    }
}
