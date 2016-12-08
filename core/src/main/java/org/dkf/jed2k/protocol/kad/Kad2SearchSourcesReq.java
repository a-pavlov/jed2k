package org.dkf.jed2k.protocol.kad;

import lombok.Getter;
import lombok.Setter;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.UInt16;
import org.dkf.jed2k.protocol.UInt64;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 15.11.2016.
 */
@Getter
@Setter
public class Kad2SearchSourcesReq extends Transaction {
    private KadId target = new KadId();
    private UInt16 startPos = new UInt16();
    private UInt64 size = new UInt64();

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return size.get(startPos.get(target.get(src)));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return size.put(startPos.put(target.put(dst)));
    }

    @Override
    public int bytesCount() {
        return target.bytesCount() + startPos.bytesCount() + size.bytesCount();
    }

    @Override
    public byte getTransactionId() {
        return Transaction.SEARCH;
    }

    @Override
    public KadId getTargetId() {
        return target;
    }
}
