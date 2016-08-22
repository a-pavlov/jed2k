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
     * reduce allocations counter and move free buffer to cache if max buffers limit greater
     * than count of current allocated buffers and cache dataSize
     * @param b - byte buffer
     * @param sessionTime - current session time when byte buffer has been released
     */
    public void deallocate(ByteBuffer b, long sessionTime) {
        assert(b != null);
        assert(freeBuffers.size() == bufferReleaseTimes.size());
        allocatedBuffersCount--;
        // add free buffer to cache only if limit not exceeded
        if (maxBuffersCount > allocatedBuffersCount + cachedBuffers()) {
            freeBuffers.addFirst(b);
            bufferReleaseTimes.addFirst(sessionTime);
        }
    }

    public int cachedBuffers() {
        return freeBuffers.size();
    }

    public int totalAllocatedBuffers() {
        return allocatedBuffersCount;
    }

    int reduceCache(int cacheSize) {
        if (cacheSize == 0) {
            freeBuffers.clear();
            bufferReleaseTimes.clear();
        } else {
            while(freeBuffers.size() > cacheSize) {
                freeBuffers.removeFirst();
                bufferReleaseTimes.removeFirst();
            }
        }

        return freeBuffers.size();
    }

    public void secondTick(long currentSessionTime) {
        // free obsolete buffers
    }

    /**
     * set max buffer count in buffer pool. if new limit greater than previous it is simply increase hi border.
     * if new limit less than previous this call will try to reduce cache to satisfy new border
     * @param maxBuffers new buffers count available for allocation in this buffer pool
     */
    public void setMaxBuffersCount(int maxBuffers) {
        assert(maxBuffers > 0);
        if (maxBuffers < maxBuffersCount) {
            // ok, max buffers count less than previous, try to reduce cache
            int newCacheSize = Math.max(cachedBuffers() - (maxBuffersCount - maxBuffers), 0);
            reduceCache(newCacheSize);
        }

        maxBuffersCount = maxBuffers;
    }
}
