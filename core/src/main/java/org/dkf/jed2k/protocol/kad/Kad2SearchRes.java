package org.dkf.jed2k.protocol.kad;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Container;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.UInt16;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 15.11.2016.
 */
public class Kad2SearchRes implements Serializable {
    private KadId kidSource = new KadId();
    private KadId kidTarget = new KadId();
    private Container<UInt16, KadSearchEntry> results = Container.makeShort(KadSearchEntry.class);

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return results.get(kidTarget.get(kidSource.get(src)));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return results.put(kidTarget.put(kidSource.put(dst)));
    }

    @Override
    public int bytesCount() {
        return kidSource.bytesCount()*2 + results.bytesCount();
    }
}
