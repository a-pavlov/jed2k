package org.dkf.jed2k;

import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;

import java.util.concurrent.Callable;

/**
 * Created by inkpot on 24.08.2016.
 */
public class AsyncRelease implements Callable<AsyncOperationResult> {

    private final Transfer transfer;

    public AsyncRelease(final Transfer t) {
        transfer = t;
    }

    @Override
    public AsyncOperationResult call() throws Exception {
        try {
            transfer.pm.releaseFile();
        } catch(JED2KException e) {
            return new AsyncReleaseResult(e.getErrorCode(), transfer);
        }

        return new AsyncReleaseResult(ErrorCode.NO_ERROR, transfer);
    }
}
