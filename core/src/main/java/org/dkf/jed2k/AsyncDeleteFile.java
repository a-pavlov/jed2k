package org.dkf.jed2k;

import java.util.concurrent.Callable;

/**
 * Created by inkpot on 24.08.2016.
 */
public class AsyncDeleteFile implements Callable<AsyncOperationResult> {
    private final Transfer transfer;

    public AsyncDeleteFile(final Transfer t) {
        transfer = t;
    }

    @Override
    public AsyncOperationResult call() throws Exception {
        transfer.pm.deleteFile();
        return new AsyncDeleteResult(transfer);
    }
}
