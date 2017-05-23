package org.dkf.jed2k;

import org.dkf.jed2k.data.PieceBlock;
import org.dkf.jed2k.exception.BaseErrorCode;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by inkpot on 15.07.2016.
 */
public class AsyncWriteResult implements AsyncOperationResult {
    List<ByteBuffer> buffers = null;
    final Transfer transfer;
    final BaseErrorCode code;
    final PieceBlock block;

    public AsyncWriteResult(final PieceBlock b, final List<ByteBuffer> buffers, final Transfer t, final BaseErrorCode ec) {
        this.block = b;
        this.buffers = buffers;
        transfer = t;
        this.code = ec;
    }

    @Override
    public void onCompleted() {
        transfer.onBlockWriteCompleted(block, buffers, getCode());
    }

    @Override
    public BaseErrorCode getCode() {
        return code;
    }
}
