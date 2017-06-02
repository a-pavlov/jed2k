package org.dkf.jed2k.pool;

import org.dkf.jed2k.Constants;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 08.07.2016.
 */
public class BufferPool extends Pool<ByteBuffer> {

    public BufferPool(int maxBuffers) {
        super(maxBuffers);
    }

    @Override
    protected ByteBuffer createObject() {
        return ByteBuffer.allocate(Constants.BLOCK_SIZE_INT);
    }
}
