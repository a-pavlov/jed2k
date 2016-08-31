package org.dkf.jed2k.protocol.client;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.*;

import java.nio.ByteBuffer;

/**
 * Created by ap197_000 on 22.08.2016.
 */
public abstract class CompressedPart<N extends UNumber> implements Serializable {
    public Hash hash = new Hash();
    public N beginOffset;
    public UInt32 compressedLength = Unsigned.uint32(0);


    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return compressedLength.get(beginOffset.get(hash.get(src)));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return compressedLength.put(beginOffset.put(hash.put(dst)));
    }

    @Override
    public int bytesCount() {
        return hash.bytesCount() + beginOffset.bytesCount() + compressedLength.bytesCount();
    }

    public int payloadSize() {
        return compressedLength.intValue();
    }
}
