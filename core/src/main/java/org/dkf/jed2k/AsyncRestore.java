package org.dkf.jed2k;

import org.dkf.jed2k.data.PieceBlock;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.concurrent.Callable;

/**
 * Created by inkpot on 24.08.2016.
 */
public class AsyncRestore implements Callable<AsyncOperationResult> {
    private final Transfer transfer;
    private final PieceBlock block;
    private final long fileSize;
    private final ByteBuffer buffer;

    public AsyncRestore(final Transfer t, final PieceBlock b, long fs, final ByteBuffer bf) {
        transfer = t;
        block = b;
        fileSize = fs;
        buffer = bf;
    }

    @Override
    public AsyncOperationResult call() throws Exception {
        try {
            return new AsyncWriteResult(block, transfer.getPieceManager().restoreBlock(block, buffer, fileSize), transfer, ErrorCode.NO_ERROR);
        } catch(JED2KException e) {
            return new AsyncWriteResult(block, new LinkedList<ByteBuffer>(){{addLast(buffer);}}, transfer, e.getErrorCode());
        }
    }
}
