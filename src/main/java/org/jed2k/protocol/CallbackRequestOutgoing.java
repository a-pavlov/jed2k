package org.jed2k.protocol;

import java.nio.ByteBuffer;
import static org.jed2k.Utils.sizeof;

public class CallbackRequestOutgoing implements Serializable {
    public int clientId = 0;

    @Override
    public ByteBuffer get(ByteBuffer src) throws ProtocolException {
        clientId = src.getInt();
        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws ProtocolException {
        return dst.putInt(clientId);
    }

    @Override
    public int size() {
        return sizeof(clientId);
    }
    
}
