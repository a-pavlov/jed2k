package org.dkf.jed2k;

import org.dkf.jed2k.exception.BaseErrorCode;

/**
 * common interface for async operation result
 * actually now only one async operation - write to disk
 * Created by inkpot on 14.07.2016.
 */
public interface AsyncOperationResult {
    /**
     * callback on operation
     */
    public void onCompleted();

    /**
     *
     * @return completion code of operation, NO_ERROR if success
     */
    public BaseErrorCode getCode();
}
