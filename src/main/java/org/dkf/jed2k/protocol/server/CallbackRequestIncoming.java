package org.dkf.jed2k.protocol.server;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.NetworkIdentifier;
import org.dkf.jed2k.protocol.Serializable;

import java.nio.ByteBuffer;

public class CallbackRequestIncoming implements Serializable {
    public NetworkIdentifier point = new NetworkIdentifier();

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return point.get(src);
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return point.put(dst);
    }

    @Override
    public int bytesCount() {
        return point.bytesCount();
    }
}
