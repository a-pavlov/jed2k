package org.jed2k.protocol.server;

import org.jed2k.protocol.PacketKey;
import org.jed2k.protocol.Serializable;

public class PacketCombiner extends org.jed2k.protocol.PacketCombiner {

    @Override
    protected Class<? extends Serializable> keyToPacket(PacketKey key) {
        return supportedPacketsServer.get(key);
    }

    @Override
    protected PacketKey classToKey(Class<? extends Serializable> clazz) {
        return struct2KeyServer.get(clazz);
    }
    
}
