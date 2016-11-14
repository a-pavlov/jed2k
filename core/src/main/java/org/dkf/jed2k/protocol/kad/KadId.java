package org.dkf.jed2k.protocol.kad;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Hash;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 14.11.2016.
 */
public class KadId extends Hash {

    public KadId() {
        super();
    }

    public KadId(final Hash h) {
        super(h);
    }

    public static KadId fromString(final String s) {
        return new KadId(Hash.fromString(s));
    }

    public static KadId fromBytes(byte[] data) {
        return new KadId(Hash.fromBytes(data));
    }

    /**
     * save/load as 4 32 bits digits in little endian save as 4 32 bits digits network byte order
     * rotate bytes in each 4 byte portion
     *
     */
    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        for (short i = 0; i < value.length; ++i) {
            byte b = src.get();
            value[(i / 4)*4 + 3 - (i % 4)] = b;
        }

        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        for (short i = 0; i < value.length; ++i) {
            dst.put(value[(i / 4) * 4 + 3 - (i % 4)]);
        }

        return dst;
    }

    @Override
    public int bytesCount() {
        return value.length;
    }
}
