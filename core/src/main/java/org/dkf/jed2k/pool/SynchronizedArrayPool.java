package org.dkf.jed2k.pool;

/**
 * Created by apavlov on 06.03.17.
 */
public class SynchronizedArrayPool extends Pool<byte[]> {
    private final int bufferSize;

    public SynchronizedArrayPool(int maxBuffers, int bufferSize) {
        super(maxBuffers);
        assert bufferSize > 0;
        this.bufferSize = bufferSize;
    }

    @Override
    public byte[] allocate() {
        assert false;
        return null;
    }

    @Override
    public void deallocate(byte[] b, long sessionTime) {
        assert false;
    }

    @Override
    public void setMaxBuffersCount(int maxBuffers) {
        assert false;
    }

    public synchronized byte[] allocateSync() {
        return super.allocate();
    }

    public synchronized void deallocateSync(byte[] b, long sessionTime) {
        super.deallocate(b, sessionTime);
    }

    public synchronized void setMaxBuffersCountSync(int maxBuffers) {
        super.setMaxBuffersCount(maxBuffers);
    }

    @Override
    protected byte[] createObject() {
        return new byte[bufferSize];
    }
}
