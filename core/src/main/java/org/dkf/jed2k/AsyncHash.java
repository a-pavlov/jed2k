package org.dkf.jed2k;

import org.dkf.jed2k.protocol.Hash;

import java.util.concurrent.Callable;

/**
 * Created by ap197_000 on 16.08.2016.
 */
public class AsyncHash implements Callable<AsyncOperationResult> {
    private final Transfer transfer;
    private final int pieceIndex;

    public AsyncHash(final Transfer t, int pieceIndex) {
        transfer = t;
        this.pieceIndex = pieceIndex;
    }

    @Override
    public AsyncOperationResult call() throws Exception {
        Hash h = transfer.getPieceManager().hashPiece(pieceIndex);
        return new AsyncHashResult(h, transfer, pieceIndex);
    }
}
