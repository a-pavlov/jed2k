package org.jed2k.protocol;

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
    public Buffer get(Buffer src) throws ProtocolException {
        ip = src.getInt();
        port = src.getShort();
        return src;
    }

    @Override
    public Buffer put(Buffer dst) throws ProtocolException {
        return dst.put(ip).put(port);
    }
}