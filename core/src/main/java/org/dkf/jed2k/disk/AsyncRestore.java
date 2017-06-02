package org.dkf.jed2k.disk;

import org.dkf.jed2k.Transfer;
import org.dkf.jed2k.data.PieceBlock;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;

import java.nio.ByteBuffer;
import java.util.LinkedList;

/**
 * Created by inkpot on 24.08.2016.
 */
public class AsyncRestore extends TransferCallable<AsyncOperationResult> {
    private final PieceBlock block;
    private final long fileSize;
    private final ByteBuffer buffer;

    public AsyncRestore(final Transfer t, final PieceBlock b, long fs, final ByteBuffer bf) {
        super(t);
        block = b;
        fileSize = fs;
        buffer = bf;
    }

    @Override
    public AsyncOperationResult call() throws Exception {
        try {
            return new AsyncWriteResult(block
                    , getTransfer().getPieceManager().restoreBlock(block, buffer, fileSize)
                    , getTransfer()
                    , ErrorCode.NO_ERROR);
        } catch(JED2KException e) {
            return new AsyncWriteResult(block
                    , new LinkedList<ByteBuffer>(){{addLast(buffer);}}
                    , getTransfer()
                    , e.getErrorCode());
        }
    }
}
