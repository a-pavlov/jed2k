package org.jed2k;

import java.nio.ByteBuffer;
import java.util.concurrent.Callable;

/**
 * Created by inkpot on 15.07.2016.
 */
public class AsyncWrite implements Callable<AsyncOperationResult> {
    private int piece = -1;
    private int offset = -1;
    private int length = -1;
    ByteBuffer buffer;

    AsyncWrite(PeerRequest request, ByteBuffer b) {
        assert(request.piece != -1);
        assert(request.offset != -1);
        assert(request.length != -1);
        assert(b != null);
        piece = request.piece;
        offset = request.offset;
        length = request.length;
        buffer = b;
    }

    @Override
    public AsyncOperationResult call() throws Exception {
        // call storage method here
        return null;
    }
}
