package org.jed2k;

import java.nio.ByteBuffer;
import java.util.LinkedList;

/**
 * Created by inkpot on 08.07.2016.
 */
public class BufferPool {
    private int maxBuffersCount = 0;
    private int allocatedBuffersCount = 0;
    LinkedList<ByteBuffer>  freeBuffers = new LinkedList<ByteBuffer>();
    LinkedList<Long> bufferReleaseTimes = new LinkedList<Long>();


    public BufferPool(int maxBuffers) {
        assert(maxBuffers > 0);
        maxBuffersCount = maxBuffers;
    }

    public ByteBuffer allocate() {
        ByteBuffer b = freeBuffers.poll();
        Long releaseTime = bufferReleaseTimes.poll();
        assert(freeBuffers.size() == bufferReleaseTimes.size());

        if (b == null && allocatedBuffersCount < maxBuffersCount) {
            b = ByteBuffer.allocate((int)Constants.BLOCK_SIZE);
        }

        if (b != null) allocatedBuffersCount++;
        return b;
    }

    /**
     *
     * @param b - byte buffer
     * @param sessionTime - current session time when byte buffer has been released
     */
    public void deallocate(ByteBuffer b, long sessionTime) {
        assert(b != null);
        assert(freeBuffers.size() == bufferReleaseTimes.size());
        allocatedBuffersCount--;
        freeBuffers.addFirst(b);
        bufferReleaseTimes.addFirst(sessionTime);
    }

    public int cachedBuffers() {
        return freeBuffers.size();
    }

    public int totalAllocatedBuffers() {
        return allocatedBuffersCount;
    }

    public void secondTick(long currentSessionTime) {
        // free obsolete buffers
    }
}
