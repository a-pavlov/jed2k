package org.dkf.jed2k.pool;

import org.dkf.jed2k.Constants;

import java.nio.ByteBuffer;
import java.util.LinkedList;

/**
 * Created by inkpot on 08.07.2016.
 */
public class BufferPool extends Pool<ByteBuffer> {
    private int maxBuffersCount = 0;
    private int allocatedBuffersCount = 0;
    private int maxAllocatedCount = 0;
    LinkedList<ByteBuffer>  freeBuffers = new LinkedList<ByteBuffer>();
    LinkedList<Long> bufferReleaseTimes = new LinkedList<Long>();


    public BufferPool(int maxBuffers) {
        super(maxBuffers);
        maxBuffersCount = maxBuffers;
    }

    @Override
    protected ByteBuffer createObject() {
        return ByteBuffer.allocate(Constants.BLOCK_SIZE_INT);
    }
}
