package org.jed2k.test;

import org.jed2k.BufferPool;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.LinkedList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by inkpot on 08.07.2016.
 */
public class BufferPoolTest {
    @Test
    public void testBufferPool() {
        BufferPool bp = new BufferPool(4);
        assertEquals(0, bp.totalAllocatedBuffers());
        assertEquals(0, bp.cachedBuffers());
        LinkedList<ByteBuffer> allocated = new LinkedList<ByteBuffer>();
        for(int i = 0; i < 4; ++i) {
            allocated.add(bp.allocate());
            assertTrue(allocated.getLast() != null);
            assertEquals(i+1, bp.totalAllocatedBuffers());
            assertEquals(0, bp.cachedBuffers());
        }

        assertTrue(bp.allocate() == null);
        for(int i = 0; i < 2; ++i) {
            bp.deallocate(allocated.poll(), i);
            assertEquals(i+1, bp.cachedBuffers());
            assertEquals(4-i-1, bp.totalAllocatedBuffers());
        }

        assertTrue(bp.allocate() != null);
        assertEquals(1, bp.cachedBuffers());
        assertEquals(3, bp.totalAllocatedBuffers());
    }
}
