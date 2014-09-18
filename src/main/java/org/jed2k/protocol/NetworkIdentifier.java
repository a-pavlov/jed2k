package org.jed2k.protocol;

import static org.jed2k.protocol.Unsigned.uint16;
import static org.jed2k.protocol.Unsigned.uint32;

public final class NetworkIdentifier implements Serializable {
    public final UInt32 client_id = uint32();
    public final UInt16 port = uint16();

    @Override
    public Buffer get(Buffer src) {
        return src.get(client_id).get(port);
    }

    @Override
    public Buffer put(Buffer dst) {
        return dst.put(client_id).put(port);
    }
}