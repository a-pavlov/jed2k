package org.jed2k.protocol;
import static org.jed2k.Utils.sizeof;

import java.nio.ByteBuffer;

public final class NetworkIdentifier implements Serializable {
    public int ip = 0;
    public short port = 0;

    public NetworkIdentifier() {
    }
    
    public NetworkIdentifier(int ip, short port) {
        this.ip = ip;
        this.port = port;
    }
    
    @Override
    public ByteBuffer get(ByteBuffer src) throws ProtocolException {
        ip = src.getInt();
        port = src.getShort();
        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws ProtocolException {
        return dst.putInt(ip).putShort(port);
    }
    
    @Override
    public int size() {
        return sizeof(ip) + sizeof(port);
    }
}