package org.dkf.jed2k.alert;

import org.dkf.jed2k.protocol.Hash;

/**
 * Created by inkpot on 17.08.2016.
 */
public class TransferFinishedAlert extends TransferAlert {

    public TransferFinishedAlert(final Hash h) {
        super(h);
    }

    @Override
    public String toString() {
        return "transfer finished " + super.toString();
    }
}
