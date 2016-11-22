package org.dkf.jed2k.protocol.kad;

import lombok.Builder;
import lombok.Getter;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.UInt16;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 15.11.2016.
 */
@Getter
@Builder
public class Kad2SearchKeysReq implements Serializable {
    private KadId kid = new KadId();
    private UInt16 startPos = new UInt16();

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return startPos.get(kid.get(src));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return startPos.put(kid.put(dst));
    }

    @Override
    public int bytesCount() {
        return kid.bytesCount() + startPos.bytesCount();
    }
}
