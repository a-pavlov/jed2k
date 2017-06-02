package org.dkf.jed2k.disk;

import org.dkf.jed2k.Transfer;
import org.dkf.jed2k.data.PieceBlock;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;

import java.nio.ByteBuffer;
import java.util.LinkedList;

/**
 * callable for async write to disk
 * uses transfer's piece manager to execute write from dedicated thread
 * Created by inkpot on 15.07.2016.
 */
public class AsyncWrite extends TransferCallable<AsyncOperationResult> {
    final PieceBlock block;
    final ByteBuffer buffer;

    // TODO - check parameters here, most likely no need peer request, use piece block here
    public AsyncWrite(final PieceBlock block, final ByteBuffer b, final Transfer t) {
        super(t);
        assert b != null;
        assert block != null;
        assert t != null;
        boolean hasRemaining = b.hasRemaining();
        assert hasRemaining;
        this.block = block;
        buffer = b;
    }

    @Override
    public AsyncOperationResult call() throws Exception {
        try {
            return new AsyncWriteResult(block
                    , getTransfer().getPieceManager().writeBlock(block, buffer)
                    , getTransfer(), ErrorCode.NO_ERROR);
        } catch(JED2KException e) {
            return new AsyncWriteResult(block
                    , new LinkedList<ByteBuffer>(){{addLast(buffer);}}
                    , getTransfer()
                    , e.getErrorCode());
        }
    }
}
