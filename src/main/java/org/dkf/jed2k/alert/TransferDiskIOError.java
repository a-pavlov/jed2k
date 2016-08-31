package org.dkf.jed2k.alert;

import org.dkf.jed2k.exception.BaseErrorCode;
import org.dkf.jed2k.protocol.Hash;

/**
 * Created by inkpot on 18.08.2016.
 */
public class TransferDiskIOError extends TransferAlert {
    public final BaseErrorCode ec;

    public TransferDiskIOError(final Hash h, final BaseErrorCode ec) {
        super(h);
        this.ec = ec;
    }

    @Override
    public String toString() {
        return "transfer " + ec.toString() + " i/o error " + ec.getDescription();
    }
}
