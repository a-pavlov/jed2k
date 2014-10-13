package org.jed2k.protocol;

public class ServerList implements Serializable {

    @Override
    public Buffer get(Buffer src) throws ProtocolException {
        return src;
    }

    @Override
    public Buffer put(Buffer dst) throws ProtocolException {
        return dst;
    }

    @Override
    public int size() {        
        return 0;
    }
    
}