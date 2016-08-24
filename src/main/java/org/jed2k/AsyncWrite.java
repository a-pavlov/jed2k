package org.jed2k;

import org.jed2k.data.PieceBlock;
import org.jed2k.exception.ErrorCode;
import org.jed2k.exception.JED2KException;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.concurrent.Callable;

/**
 * callable for async write to disk
 * uses transfer's piece manager to execute write from dedicated thread
 * Created by inkpot on 15.07.2016.
 */
public class AsyncWrite implements Callable<AsyncOperationResult> {
    final PieceBlock block;
    final ByteBuffer buffer;
    final Transfer transfer;

    // TODO - check parameters here, most likely no need peer request, use piece block here
    public AsyncWrite(final PieceBlock block, final ByteBuffer b, final Transfer t) {
        assert(b != null);
        assert(block != null);
        assert(t != null);
        assert(b.hasRemaining());
        this.block = block;
        buffer = b;
        transfer = t;
    }

    @Override
    public AsyncOperationResult call() throws Exception {
        try {
            return new AsyncWriteResult(block, transfer.pm.writeBlock(block, buffer), transfer, ErrorCode.NO_ERROR);
        } catch(JED2KException e) {
            return new AsyncWriteResult(block, new LinkedList<ByteBuffer>(){{addLast(buffer);}}, transfer, e.getErrorCode());
        }
    }
}
