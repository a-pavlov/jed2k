package org.dkf.jed2k.protocol.kad;

import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.PacketHeader;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * Created by inkpot on 22.11.2016.
 * special header for UDP Kademlia packets
 * has no size field
 */
public class KadPacketHeader extends PacketHeader {

    public static int KAD_SIZE = 2; // 1 byte protocol + 1 byte packet

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        try {
            protocol = src.get();
            packet = src.get();
            size = 0;
            return src;
        } catch(BufferUnderflowException e) {
            throw new JED2KException(ErrorCode.BUFFER_UNDERFLOW_EXCEPTION);
        } catch(Exception e) {
            throw new JED2KException(ErrorCode.PACKET_HEADER_EXTRACT_ERROR);
        }
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) {
        return dst.put(protocol).put(packet);
    }

    @Override
    public int bytesCount() {
        return KAD_SIZE;
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
