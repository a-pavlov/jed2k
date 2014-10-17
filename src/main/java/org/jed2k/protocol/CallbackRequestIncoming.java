package org.jed2k.protocol;

import java.nio.ByteBuffer;

public class CallbackRequestIncoming implements Serializable {
    public NetworkIdentifier point = new NetworkIdentifier();
    
    @Override
    public ByteBuffer get(ByteBuffer src) throws ProtocolException {
        return point.get(src);
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws ProtocolException {
        return point.put(dst);        
    }

    @Override
    public int size() {
        return point.size();
    }   
}
