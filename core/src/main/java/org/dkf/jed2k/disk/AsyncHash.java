package org.dkf.jed2k.disk;

import org.dkf.jed2k.Transfer;
import org.dkf.jed2k.protocol.Hash;

/**
 * Created by ap197_000 on 16.08.2016.
 */
public class AsyncHash extends TransferCallable<AsyncOperationResult> {
    private final int pieceIndex;

    public AsyncHash(final Transfer t, int pieceIndex) {
        super(t);
        this.pieceIndex = pieceIndex;
    }

    @Override
    public AsyncOperationResult call() throws Exception {
        Hash h = getTransfer().getPieceManager().hashPiece(pieceIndex);
        return new AsyncHashResult(h, getTransfer(), pieceIndex);
    }
}
