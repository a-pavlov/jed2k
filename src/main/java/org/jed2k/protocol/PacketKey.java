package org.jed2k.protocol;

public class PacketKey implements Comparable<PacketKey> {
    public final byte protocol;
    public final byte packet;

    PacketKey(byte protocol, byte packet) {        
        this.protocol   = protocol;
        this.packet     = packet;
    }

    @Override
    public int compareTo(PacketKey pk) {
        if (protocol > pk.protocol) return 1;
        if (protocol < pk.protocol) return -1;
        if (packet > pk.packet) return 1;
        if (packet < pk.packet) return -1;
        return 0;
    }
}