package org.jed2k.protocol.server;

import java.nio.ByteBuffer;
import static org.jed2k.Utils.sizeof;
import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.Serializable;

public class CallbackRequest implements Serializable {
    public int clientId = 0;

    public CallbackRequest(int c) {
        clientId = c;
    }

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        clientId = src.getInt();
        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return dst.putInt(clientId);
    }

    @Override
    public int bytesCount() {
        return sizeof(clientId);
    }

}
