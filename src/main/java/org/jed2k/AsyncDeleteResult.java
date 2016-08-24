package org.jed2k;

import org.jed2k.exception.BaseErrorCode;
import org.jed2k.exception.ErrorCode;

/**
 * Created by inkpot on 24.08.2016.
 */
public class AsyncDeleteResult implements AsyncOperationResult {
    private final Transfer transfer;


    public AsyncDeleteResult(final Transfer t) {
        transfer = t;
    }

    @Override
    public void onCompleted() {
        // temporary do nothing
    }

    @Override
    public BaseErrorCode getCode() {
        return ErrorCode.NO_ERROR;
    }
}
