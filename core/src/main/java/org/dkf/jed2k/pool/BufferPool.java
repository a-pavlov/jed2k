package org.dkf.jed2k.pool;

import org.dkf.jed2k.Constants;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.slf4j.Logger;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 08.07.2016.
 */
public class BufferPool extends Pool<ByteBuffer> {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(BufferPool.class);

    public BufferPool(int maxBuffers) {
        super(maxBuffers);
    }

    @Override
    public void deallocate(ByteBuffer buffer, long sessionTime) {
        assert buffer != null;
        buffer.clear();
        super.deallocate(buffer, sessionTime);
    }

    @Override
    protected ByteBuffer createObject() throws JED2KException {
        try {
            return ByteBuffer.allocate(Constants.BLOCK_SIZE_INT);
        } catch(OutOfMemoryError e) {
            log.error("Buffer pool allocation {} raised out of memory error {}"
                    , Constants.BLOCK_SIZE_INT
                    , e.getMessage());
            throw new JED2KException(ErrorCode.NO_MEMORY);
        } catch(Exception e) {
            log.error("Buffer pool allocation {} raised error {}"
                    , Constants.BLOCK_SIZE_INT
                    , e.getMessage());
            throw new JED2KException(ErrorCode.INTERNAL_ERROR);
        }
    }
}
