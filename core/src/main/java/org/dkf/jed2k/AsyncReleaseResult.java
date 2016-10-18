package org.dkf.jed2k;

import org.dkf.jed2k.exception.BaseErrorCode;

/**
 * Created by inkpot on 24.08.2016.
 */
public class AsyncReleaseResult implements AsyncOperationResult {
    final BaseErrorCode code;
    final Transfer transfer;

    AsyncReleaseResult(final BaseErrorCode c, final Transfer t) {
        code = c;
        transfer = t;
    }

    @Override
    public void onCompleted() {
        transfer.onReleaseFile(code);
    }

    @Override
    public BaseErrorCode getCode() {
        return code;
    }
}
