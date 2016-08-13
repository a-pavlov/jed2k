package org.jed2k;

import org.jed2k.protocol.Hash;

import java.nio.ByteBuffer;
import java.util.LinkedList;

/**
 * Created by inkpot on 15.07.2016.
 */
public class AsyncWriteResult implements AsyncOperationResult {
    LinkedList<ByteBuffer>  buffers = null;
    Hash hash = null;
    final Transfer transfer;

    public AsyncWriteResult(final Transfer t) {
        transfer = t;
    }

    @Override
    public void onCompleted() {
        transfer.onBlockWriteCompleted(null, buffers, hash);
    }
}
