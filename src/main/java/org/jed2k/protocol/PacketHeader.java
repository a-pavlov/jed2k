package org.jed2k.protocol;
import static org.jed2k.Utils.byte2String;

import java.nio.ByteBuffer;

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
    
    public void reset(PacketKey key, int size) {
        this.protocol = key.protocol;
        this.size = size;
        this.packet = key.packet;
    }

    @Override
    public String toString() {
        return "packet header {" + byte2String(protocol) + ":" + size + ":" + byte2String(packet) + "}";
    }

    @Override
    public ByteBuffer get(ByteBuffer src) throws ProtocolException {
        protocol = src.get();
        size = src.getInt();
        packet = src.get();
        
        if (!isDefined()) {
            throw new ProtocolException("Incorrect packet header content protocol: " + byte2String(protocol) + " opcode " + byte2String(packet));
        }
        
        if (size > MAX_SIZE || size < 0) {
            throw new ProtocolException("Packet size too large: " + size);
        }
        
        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws ProtocolException {
        return dst.put(protocol).putInt(size).put(packet);
    }
    
    public final int size() {
        return 6;
    }
    
    public final PacketKey key() {
        assert(isDefined());
        return new PacketKey(protocol, packet);
    }
};