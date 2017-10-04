package org.dkf.jed2k.test;

import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.pool.BufferPool;
import org.dkf.jed2k.pool.Pool;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.LinkedList;

import static junit.framework.Assert.*;

/**
 * Created by inkpot on 08.07.2016.
 */
public class BufferPoolTest {

    private static class NoAllocator extends Pool<ByteBuffer> {

        public NoAllocator(int maxBuffers) {
            super(maxBuffers);
        }

        @Override
        protected ByteBuffer createObject() throws JED2KException {
            throw new JED2KException(ErrorCode.NO_MEMORY);
        }
    }

    @Test(expected = JED2KException.class)
    public void testTrivialNoMemory() throws JED2KException {
        BufferPool bp = new BufferPool(2);
        assertEquals(0, bp.getAllocatedBuffersCount());
        assertEquals(0, bp.getCachedBuffersCount());
        bp.allocate();
        bp.allocate();
        bp.allocate();
    }

    @Test(expected = JED2KException.class)
    public void testNoMemorySystem() throws JED2KException {
        Pool bp = new NoAllocator(2);
        assertEquals(0, bp.getAllocatedBuffersCount());
        assertEquals(0, bp.getCachedBuffersCount());
        bp.allocate();
    }

    @Test
    public void testBufferPool() throws JED2KException {
        BufferPool bp = new BufferPool(4);
        assertEquals(0, bp.getAllocatedBuffersCount());
        assertEquals(0, bp.getCachedBuffersCount());
        LinkedList<ByteBuffer> allocated = new LinkedList<ByteBuffer>();
        for(int i = 0; i < 4; ++i) {
            allocated.add(bp.allocate());
            assertTrue(allocated.getLast() != null);
            assertEquals(i+1, bp.getAllocatedBuffersCount());
            assertEquals(0, bp.getCachedBuffersCount());
        }

        boolean alloc = true;
        try {
            assertTrue(bp.allocate() == null);
        } catch(JED2KException e) {
            alloc = false;
        }

        assertFalse(alloc);

        for(int i = 0; i < 2; ++i) {
            bp.deallocate(allocated.poll(), i);
            assertEquals(i+1, bp.getCachedBuffersCount());
            assertEquals(4-i-1, bp.getAllocatedBuffersCount());
        }

        assertTrue(bp.allocate() != null);
        assertEquals(1, bp.getCachedBuffersCount());
        assertEquals(3, bp.getAllocatedBuffersCount());
    }

    @Test
    public void testBufferPoolReduce() throws JED2KException {
        BufferPool bp = new BufferPool(10);
        LinkedList<ByteBuffer> allocated = new LinkedList<ByteBuffer>();
        for(int i = 0; i < 6; ++i) {
            allocated.add(bp.allocate());
            assertTrue(allocated.getLast() != null);
        }

        bp.setMaxBuffersCount(4);
        boolean alloc = true;
        try {
            assertTrue(bp.allocate() == null);
        } catch (JED2KException e) {
            alloc = false;
        }

        assertFalse(alloc);

        while(allocated.size() > 2) {
            bp.deallocate(allocated.poll(), 10L);
        }

        assertEquals(2, bp.getCachedBuffersCount());
        for(int i = 0; i < 2; ++i) {
            allocated.add(bp.allocate());
            assertTrue(allocated.getLast() != null);
        }

        alloc = true;
        try {
            assertTrue(bp.allocate() == null);
        } catch(JED2KException e) {
            alloc = false;
        }

        assertFalse(alloc);
        assertEquals(0, bp.getCachedBuffersCount());
    }

    @Test
    public void testBufferPoolReduceFull() throws JED2KException {
        BufferPool bp = new BufferPool(6);
        LinkedList<ByteBuffer> allocated = new LinkedList<ByteBuffer>();
        for(int i = 0; i < 6; ++i) {
            allocated.add(bp.allocate());
            assertTrue(allocated.getLast() != null);
        }

        while(!allocated.isEmpty()) {
            bp.deallocate(allocated.poll(), 10L);
        }

        assertEquals(6, bp.getCachedBuffersCount());
        bp.setMaxBuffersCount(2);
        assertEquals(2, bp.getCachedBuffersCount());
        assertEquals(0, bp.getAllocatedBuffersCount());
        bp.setMaxBuffersCount(8);

        for(int i = 0; i < 6; ++i) {
            allocated.add(bp.allocate());
            assertTrue(allocated.getLast() != null);
        }

        assertEquals(6, bp.getAllocatedBuffersCount());
        assertEquals(0, bp.getCachedBuffersCount());

        while(allocated.size() > 2) {
            bp.deallocate(allocated.poll(), 10L);
        }

        assertEquals(4, bp.getCachedBuffersCount());
        bp.setMaxBuffersCount(2);
        assertEquals(0, bp.getCachedBuffersCount());
        assertEquals(2, bp.getAllocatedBuffersCount());
        boolean alloc = true;
        try {
            assertTrue(bp.allocate() == null);
        } catch(JED2KException e) {
            alloc = false;
        }

        assertFalse(alloc);
    }

    @Test
    public void testMemoryAllocation() throws JED2KException {
        BufferPool pool = new BufferPool(100);
        LinkedList<ByteBuffer> buffers = new LinkedList<>();
        for(int i = 0; i < 100; ++i) {
            buffers.add(pool.allocate());
        }

        for(final ByteBuffer buffer: buffers) {
            pool.deallocate(buffer, 100);
        }

        buffers.clear();
    }
}
