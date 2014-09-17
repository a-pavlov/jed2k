package org.jed2k.protocol;

import static org.jed2k.protocol.Unsigned.uint8;

public final class NetworkIdentifier implements Serializable {
    public final UInt8 client_id = uint8();
    public final UInt8 port = uint8();

    @Override
    public Buffer get(Buffer src) {
        return src.get(client_id).get(port);
    }

    @Override
    public Buffer put(Buffer dst) {
        return dst.put(client_id).put(port);        
    }
}