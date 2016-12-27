package org.dkf.jed2k;

import lombok.Data;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.kad.NodeEntry;
import org.dkf.jed2k.protocol.Container;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.UInt32;
import org.dkf.jed2k.protocol.kad.KadId;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 18.12.2016.
 */
@Data
public class DhtInitialData implements Serializable {
    private KadId target = new KadId();
    private Container<UInt32, NodeEntry> entries = Container.makeInt(NodeEntry.class);

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return entries.get(target.get(src));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return entries.put(target.put(dst));
    }

    @Override
    public int bytesCount() {
        return target.bytesCount() + entries.bytesCount();
    }
};
