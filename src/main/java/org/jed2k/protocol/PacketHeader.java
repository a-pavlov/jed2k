package org.jed2k.protocol;

public class PacketHeader implements Serializable {
    public static byte OP_UNDEFINED     = (byte)0;
    public static byte OP_EDONKEYHEADER = (byte)0xE3;
    public static byte OP_EDONKEYPROT   = (byte)0xE3;
    public static byte OP_PACKEDPROT    = (byte)0xD4;
    public static byte OP_EMULEPROT     = (byte)0xC5;
    public static int MAX_SIZE = 1000000;
    
    public byte protocol    = OP_UNDEFINED;
    public int size         = 0;
    public byte packet      = 0;

    public final boolean isDefined() {
        return protocol != OP_UNDEFINED && packet != OP_UNDEFINED;
    }
    
    public void reset() {
        protocol = OP_UNDEFINED;
        size = 0;
        packet = OP_UNDEFINED;
    }

    @Override
    public String toString() {
        return new String("Protocol: " + protocol + " size: " + " packet: " + packet);
    }

    @Override
    public Buffer get(Buffer src) throws ProtocolException {
        protocol = src.getByte();
        size = src.getInt();
        packet = src.getByte();
        
        if (!isDefined()) {
            throw new ProtocolException("Incorrect packet header content");
        }
        
        if (size > MAX_SIZE || size < 0) {
            throw new ProtocolException("Packet size too large");
        }
        
        return src;
    }

    @Override
    public Buffer put(Buffer dst) throws ProtocolException {
        return dst.put(protocol).put(size).put(packet);
    }
    
    public final int size() {
        return 6;
    }
    
    public final PacketKey key() {
        assert(isDefined());
        return new PacketKey(protocol, packet);
    }
};