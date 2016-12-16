package org.dkf.jed2k.protocol.kad;

import org.dkf.jed2k.Utils;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.UInt16;
import org.dkf.jed2k.protocol.Unsigned;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 16.12.2016.
 */
public class Kad2FirewalledReq implements Serializable {
    UInt16 portTcp = Unsigned.uint16();
    KadId id = new KadId();
    byte options;

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        options = id.get(portTcp.get(src)).get();
        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return id.put(portTcp.put(dst)).put(options);
    }

    @Override
    public int bytesCount() {
        return portTcp.bytesCount() + id.bytesCount() + Utils.sizeof(options);
    }
}
