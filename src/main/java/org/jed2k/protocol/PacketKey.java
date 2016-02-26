package org.jed2k.protocol;

import static org.jed2k.Utils.byte2String;

public class PacketKey implements Comparable<PacketKey> {
    public final byte protocol;
    public final byte packet;

    public PacketKey(byte protocol, byte packet) {        
        this.protocol   = protocol;
        this.packet     = packet;
    }
    
    public static PacketKey pk(byte protocol, byte packet) {
        return new PacketKey(protocol, packet);
    }

    @Override
    public int compareTo(PacketKey pk) {
        if (protocol > pk.protocol) return 1;
        if (protocol < pk.protocol) return -1;
        if (packet > pk.packet) return 1;
        if (packet < pk.packet) return -1;
        return 0;
    }
    
    @Override
    public String toString() {
        return "PK {" + byte2String(protocol) + ":" + byte2String(packet) + "}";
    }
    
    @Override
    public int hashCode() {
        /**
         * full hash code value*max(hi byte) + lo byte 
         */
        return (int)protocol*255 + (int)packet;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof PacketKey) {
            PacketKey p = (PacketKey)o;
            return this.compareTo(p) == 0;
        }
        
        return false;
    }
}
