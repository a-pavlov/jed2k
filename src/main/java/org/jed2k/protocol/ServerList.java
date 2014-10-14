package org.jed2k.protocol;

import java.util.ArrayList;
import static org.jed2k.protocol.Unsigned.uint8;

public class ServerList extends ContainerHolder<UInt8, NetworkIdentifier> {

    public ServerList() {
        super(uint8(), new ArrayList<NetworkIdentifier>(), NetworkIdentifier.class);
    }

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