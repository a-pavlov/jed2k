package org.dkf.jed2k;

import org.dkf.jed2k.exception.BaseErrorCode;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.protocol.Hash;

/**
 * Created by ap197_000 on 16.08.2016.
 */
public class AsyncHashResult implements AsyncOperationResult {
    final Hash hash;
    final Transfer transfer;
    final int piece;

    public AsyncHashResult(final Hash h, final Transfer t, final int pieceIndex) {
        hash = h;
        transfer = t;
        piece = pieceIndex;
    }

    @Override
    public void onCompleted() {
        transfer.onPieceHashCompleted(piece, hash);
    }

    @Override
    public BaseErrorCode getCode() {
        return ErrorCode.NO_ERROR;
    }
}
