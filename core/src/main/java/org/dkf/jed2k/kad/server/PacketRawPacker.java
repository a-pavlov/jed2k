package org.dkf.jed2k.kad.server;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.UInt16;
import org.dkf.jed2k.protocol.Unsigned;

import java.nio.ByteBuffer;

/**
 * Created by apavlov on 10.04.17.
 */
public class PacketRawPacker {
    private final ByteBuffer buffer;
    private final Serializable header;
    private int blocksCount = 0;


    public PacketRawPacker(final ByteBuffer buffer, final Serializable header) {
        this.buffer = buffer;
        this.header = header;
    }

    public boolean hasSpace(int size) {
        return buffer.remaining() >= size;
    }

    public boolean isEmpty() {
        return blocksCount == 0;
    }

    /**
     * add data to buffer, buffer must have enough space
     * @param data - raw data with some structure inside
     */
    public void putBlock(final byte[] data) throws JED2KException {
        assert data != null;
        assert buffer != null;
        // on first block write header and counter
        if (blocksCount == 0) {
            UInt16 count = Unsigned.uint16();
            assert hasSpace(count.bytesCount() + header.bytesCount());
            count.put(header.put(buffer));
        }
        assert hasSpace(data.length);
        blocksCount++;
        buffer.put(data);
    }

    /**
     *
     * @return
     * @throws JED2KException
     */
    public ByteBuffer releaseBuffer() throws JED2KException {
        int pos = buffer.position();
        UInt16 count = Unsigned.uint16(blocksCount);
        buffer.position(header.bytesCount());
        count.put(buffer);
        buffer.position(pos);
        buffer.flip();
        blocksCount = 0;
        return buffer;
    }
}
