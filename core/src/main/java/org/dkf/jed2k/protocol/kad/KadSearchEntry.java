package org.dkf.jed2k.protocol.kad;

import lombok.Getter;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Container;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.UInt8;
import org.dkf.jed2k.protocol.tag.Tag;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 15.11.2016.
 */
@Getter
public class KadSearchEntry implements Serializable {
    private KadId kid = new KadId();
    private Container<UInt8, Tag> info = Container.makeByte(Tag.class);

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return info.get(kid.get(src));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return info.put(kid.put(dst));
    }

    @Override
    public int bytesCount() {
        return kid.bytesCount() + info.bytesCount();
    }
}
