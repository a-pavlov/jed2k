package org.dkf.jed2k.disk;

import org.dkf.jed2k.Transfer;
import org.dkf.jed2k.exception.BaseErrorCode;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by inkpot on 24.08.2016.
 */
public class AsyncReleaseResult implements AsyncOperationResult {
    final BaseErrorCode code;
    final Transfer transfer;
    final List<ByteBuffer> buffers;
    final boolean deleteFile;

    AsyncReleaseResult(final BaseErrorCode c, final Transfer t, final List<ByteBuffer> buffers, boolean deleteFile) {
        code = c;
        transfer = t;
        this.buffers = buffers;
        this.deleteFile = deleteFile;
    }

    @Override
    public void onCompleted() {
        transfer.onReleaseFile(code, buffers, deleteFile);
    }

    @Override
    public BaseErrorCode getCode() {
        return code;
    }
}
