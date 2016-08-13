package org.jed2k;

/**
 * common interface for async operation result
 * actually now only one async operation - write to disk
 * Created by inkpot on 14.07.2016.
 */
public interface AsyncOperationResult {
    public void onCompleted();
}
