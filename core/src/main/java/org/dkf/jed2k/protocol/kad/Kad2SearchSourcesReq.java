package org.dkf.jed2k.protocol.kad;

import lombok.Getter;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.UInt16;
import org.dkf.jed2k.protocol.UInt64;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 15.11.2016.
 */
@Getter
public class Kad2SearchSourcesReq implements Serializable {
    private KadId kid = new KadId();
    private UInt16 startPos = new UInt16();
    private UInt64 size = new UInt64();

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return size.get(startPos.get(kid.get(src)));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return size.put(startPos.put(kid.put(dst)));
    }

    @Override
    public int bytesCount() {
        return kid.bytesCount() + startPos.bytesCount() + size.bytesCount();
    }
}
