package org.jed2k.protocol;

public class ClientPacketCombiner extends PacketCombiner {

    @Override
    protected Class<? extends Serializable> keyToPacket(PacketKey key) {
        return supportedPacketsClient.get(key);
    }

    @Override
    protected PacketKey classToKey(Class<? extends Serializable> clazz) {
        return struct2KeyClient.get(clazz);
    }   
}
