package org.dkf.jed2k.pool;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;

import java.util.LinkedList;

/**
 * Created by apavlov on 06.03.17.
 */
@Getter
@Slf4j
public abstract class Pool<T> {
    private int maxBuffersCount = 0;
    private int allocatedBuffersCount = 0;
    private int maxAllocatedCount = 0;
    LinkedList<T> freeBuffers = new LinkedList<>();
    LinkedList<Long> bufferReleaseTimes = new LinkedList<Long>();


    public Pool(int maxBuffers) {
        assert(maxBuffers > 0);
        maxBuffersCount = maxBuffers;
    }

    protected abstract T createObject() throws JED2KException ;

    public T allocate() throws JED2KException {
        T b = freeBuffers.poll();
        bufferReleaseTimes.poll();
        assert(freeBuffers.size() == bufferReleaseTimes.size());

        if (b == null && allocatedBuffersCount < maxBuffersCount) {
            b = createObject();
        }

        if (b != null) {
            allocatedBuffersCount++;
            maxAllocatedCount = Math.max(maxAllocatedCount, allocatedBuffersCount);
        } else {
            log.warn("Pool allocate no memory, allocated buffers count {} max buffers {}"
                    , allocatedBuffersCount
                    , maxAllocatedCount);
            throw new JED2KException(ErrorCode.NO_MEMORY);
        }

        return b;
    }

    /**
     * reduce allocations counter and move free buffer to cache if max buffers limit greater
     * than count of current allocated buffers and cache dataSize
     * @param b - byte buffer
     * @param sessionTime - current session time when byte buffer has been released
     */
    public void deallocate(T b, long sessionTime) {
        assert(b != null);
        assert(freeBuffers.size() == bufferReleaseTimes.size());
        allocatedBuffersCount--;
        // add free buffer to cache only if limit not exceeded
        if (maxBuffersCount > allocatedBuffersCount + getCachedBuffersCount()) {
            freeBuffers.addFirst(b);
            bufferReleaseTimes.addFirst(sessionTime);
        }
    }

    public int getCachedBuffersCount() {
        return freeBuffers.size();
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
            int newCacheSize = Math.max(getCachedBuffersCount() - (maxBuffersCount - maxBuffers), 0);
            reduceCache(newCacheSize);
        }

        maxBuffersCount = maxBuffers;
    }

    @Override
    public String toString() {
        return "buffer pool max{" + maxBuffersCount + "} allocated/maxallocated {" + allocatedBuffersCount + "/" + maxAllocatedCount + "} free {" + freeBuffers.size() + "}";
    }
}
