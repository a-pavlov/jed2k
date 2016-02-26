package org.jed2k.protocol.server;

import java.nio.ByteBuffer;
import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.NetworkIdentifier;
import org.jed2k.protocol.Serializable;

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
