package org.jed2k.protocol;

import java.nio.ByteBuffer;

import org.jed2k.Utils;
import org.jed2k.exception.JED2KException;

public class ClientQueueRanking implements Serializable {
    public short rank = 0;

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        rank = src.getShort();
        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return dst.putShort(rank);
    }

    @Override
    public int bytesCount() {
        return Utils.sizeof(rank);
    }
}
