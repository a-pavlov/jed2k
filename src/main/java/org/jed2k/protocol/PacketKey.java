package org.jed2k.protocol;

import static org.jed2k.protocol.Unsigned.uint8;

public class PacketKey implements Comparable<PacketKey> {
    public final UInt8 protocol = uint8();
    public final UInt8 packet   = uint8();

    PacketKey(byte protocol, byte packet) {
        this.protocol.assign(protocol);
        this.packet.assign(packet);
    }

    @Override
    public int compareTo(PacketKey pk) {
        if (this.protocol.compareTo(pk.protocol) == 1) return 1;
        if (this.protocol.compareTo(pk.protocol) == -1) return -1;
        if (this.packet.compareTo(pk.packet) == 1) return 1;
        if (this.packet.compareTo(pk.packet) == -1) return -1;
        return 0;
    }
}