package org.dkf.jed2k.protocol.kad;

import lombok.Getter;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Container;
import org.dkf.jed2k.protocol.UInt16;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 15.11.2016.
 */
@Getter
public class Kad2SearchRes extends Transaction {
    private KadId source = new KadId();
    private KadId target = new KadId();
    private Container<UInt16, KadSearchEntry> results = Container.makeShort(KadSearchEntry.class);

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return results.get(target.get(source.get(src)));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return results.put(target.put(source.put(dst)));
    }

    @Override
    public int bytesCount() {
        return source.bytesCount()*2 + results.bytesCount();
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
