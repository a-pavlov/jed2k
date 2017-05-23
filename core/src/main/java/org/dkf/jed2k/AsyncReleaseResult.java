package org.dkf.jed2k;

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

    AsyncReleaseResult(final BaseErrorCode c, final Transfer t, final List<ByteBuffer> buffers) {
        code = c;
        transfer = t;
        this.buffers = buffers;
    }

    @Override
    public void onCompleted() {
        transfer.onReleaseFile(code, buffers);
    }

    @Override
    public BaseErrorCode getCode() {
        return code;
    }
}
