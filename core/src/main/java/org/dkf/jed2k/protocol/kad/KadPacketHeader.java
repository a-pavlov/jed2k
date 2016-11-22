package org.dkf.jed2k.protocol.kad;

import org.dkf.jed2k.protocol.PacketHeader;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 22.11.2016.
 * special header for UDP Kademlia packets
 * has no size field
 */
public class KadPacketHeader extends PacketHeader {

    @Override
    public ByteBuffer get(ByteBuffer src) {
        protocol = src.get();
        packet = src.get();
        size = 0;
        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) {
        return dst.put(protocol).put(packet);
    }

    @Override
    public int bytesCount() {
        return 2;   // protocol + packet
    }

    /**
     *
     * @return explicitly specified size
     */
    @Override
    public int sizePacket() {
        return size;
    }
}
