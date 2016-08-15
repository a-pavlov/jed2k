package org.jed2k;

import org.jed2k.data.PeerRequest;

import java.nio.ByteBuffer;
import java.util.concurrent.Callable;

/**
 * callable for async write to disk
 * uses transfer's piece manager to execute write from dedicated thread
 * Created by inkpot on 15.07.2016.
 */
public class AsyncWrite implements Callable<AsyncOperationResult> {
    private int piece = -1;
    private long offset = -1;
    private long length = -1;
    final ByteBuffer buffer;
    final Transfer transfer;

    // TODO - check parameters here, most likely no need peer request, use piece bloc here
    public AsyncWrite(PeerRequest request, final ByteBuffer b, final Transfer t) {
        assert(request.piece != -1);
        assert(request.start != -1);
        assert(request.length != -1);
        assert(b != null);
        piece = request.piece;
        offset = request.start;
        length = request.length;
        buffer = b;
        transfer = t;
    }

    @Override
    public AsyncOperationResult call() throws Exception {
        //transfer.pm.writeBlock();
        // actual write to disk with catch exceptions
        return new AsyncWriteResult(transfer);
    }
}
