package org.jed2k.protocol;

import java.nio.ByteBuffer;
import static org.jed2k.Utils.sizeof;
import org.jed2k.exception.JED2KException;

public class CallbackRequestOutgoing implements Serializable {
    public int clientId = 0;

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
