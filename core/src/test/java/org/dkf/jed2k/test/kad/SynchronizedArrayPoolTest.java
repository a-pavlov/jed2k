package org.dkf.jed2k.test.kad;

import org.dkf.jed2k.pool.SynchronizedArrayPool;
import org.junit.Test;

import java.util.LinkedList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by apavlov on 06.03.17.
 */
public class SynchronizedArrayPoolTest {

    @Test
    public void testMemoryAllocation() {
        SynchronizedArrayPool pool = new SynchronizedArrayPool(100, 10);
        LinkedList<byte[]> buffers = new LinkedList<>();
        for(int i = 0; i < 100; ++i) {
            buffers.add(pool.allocateSync());
        }

        for(final byte[] buffer: buffers) {
            pool.deallocateSync(buffer, 100);
        }

        buffers.clear();
    }

    @Test
    public void testBufferPoolReduceFull() {
        SynchronizedArrayPool bp = new SynchronizedArrayPool(6, 10);
        LinkedList<byte[]> allocated = new LinkedList<>();
        for(int i = 0; i < 6; ++i) {
            allocated.add(bp.allocateSync());
            assertTrue(allocated.getLast() != null);
        }

        while(!allocated.isEmpty()) {
            bp.deallocateSync(allocated.poll(), 10L);
        }

        assertEquals(6, bp.getCachedBuffersCount());
        bp.setMaxBuffersCountSync(2);
        assertEquals(2, bp.getCachedBuffersCount());
        assertEquals(0, bp.getAllocatedBuffersCount());
        bp.setMaxBuffersCountSync(8);

        for(int i = 0; i < 6; ++i) {
            allocated.add(bp.allocateSync());
            assertTrue(allocated.getLast() != null);
        }

        assertEquals(6, bp.getAllocatedBuffersCount());
        assertEquals(0, bp.getCachedBuffersCount());

        while(allocated.size() > 2) {
            bp.deallocateSync(allocated.poll(), 10L);
        }

        assertEquals(4, bp.getCachedBuffersCount());
        bp.setMaxBuffersCountSync(2);
        assertEquals(0, bp.getCachedBuffersCount());
        assertEquals(2, bp.getAllocatedBuffersCount());
        assertTrue(bp.allocateSync() == null);
    }
}
