package org.dkf.jed2k.disk;

import org.dkf.jed2k.Transfer;
import org.dkf.jed2k.exception.ErrorCode;

/**
 * Created by inkpot on 24.08.2016.
 */
public class AsyncRelease extends TransferCallable<AsyncOperationResult> {
    private final boolean deleteFile;

    public AsyncRelease(final Transfer t, boolean deleteFile) {
        super(t);
        this.deleteFile = deleteFile;
    }

    @Override
    public AsyncOperationResult call() throws Exception {
        return new AsyncReleaseResult(ErrorCode.NO_ERROR
                , getTransfer()
                , getTransfer().getPieceManager().releaseFile(deleteFile)
                , deleteFile);
    }
}
