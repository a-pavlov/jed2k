package org.dkf.jed2k.alert;

import org.dkf.jed2k.protocol.Hash;

/**
 * Created by inkpot on 18.08.2016.
 */
public class TransferResumedAlert extends TransferAlert {

    public TransferResumedAlert(final Hash h) {
        super(h);
    }

    @Override
    public String toString() {
        return "transfer resumed " + super.toString();
    }
}
