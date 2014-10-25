package org.jed2k.protocol;


public class ServerPacketCombiner extends PacketCombiner {

    @Override
    protected Class<? extends Serializable> keyToPacket(PacketKey key) {
        return supportedPacketsServer.get(key);
    }

    @Override
    protected PacketKey classToKey(Class<? extends Serializable> clazz) {
        return struct2KeyServer.get(clazz);
    }
    
}
