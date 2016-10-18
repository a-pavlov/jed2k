package org.dkf.jed2k.protocol;

import org.dkf.jed2k.exception.JED2KException;

import java.nio.ByteBuffer;

import static org.dkf.jed2k.Utils.byte2String;

public class PacketHeader implements Serializable {
    public static byte OP_UNDEFINED     = (byte)0;
    public static byte OP_EDONKEYHEADER = (byte)0xE3;
    public static byte OP_EDONKEYPROT   = (byte)0xE3;
    public static byte OP_PACKEDPROT    = (byte)0xD4;
    public static byte OP_EMULEPROT     = (byte)0xC5;
    public static int SIZE = 6;

    private byte protocol    = OP_UNDEFINED;
    private int size         = 0;
    private byte packet      = 0;

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
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        protocol = src.get();
        size = src.getInt();
        packet = src.get();
        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return dst.put(protocol).putInt(size).put(packet);
    }

    public final int bytesCount() {
        return 6;
    }

    public final int sizePacket() {
        return size - 1;
    }

    public final PacketKey key() {
        assert(isDefined());
        return new PacketKey(protocol, packet);
    }
};