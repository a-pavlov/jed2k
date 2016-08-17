package org.jed2k.alert;

import org.jed2k.protocol.Hash;

/**
 * Created by inkpot on 17.08.2016.
 */
public class TransferFinishedAlert extends Alert {
    public final Hash hash;

    public TransferFinishedAlert(final Hash h) {
        hash = h;
    }

    @Override
    public Severity severity() {
        return null;
    }

    @Override
    public int category() {
        return 0;
    }
}
