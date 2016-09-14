package org.dkf.jed2k.alert;

import org.dkf.jed2k.AddTransferParams;
import org.dkf.jed2k.protocol.Hash;

/**
 * Created by ap197_000 on 14.09.2016.
 */
public class TransferResumeDataAlert extends TransferAlert {
    public final AddTransferParams trd;

    public TransferResumeDataAlert(Hash h, final AddTransferParams trd) {
        super(h);
        this.trd = trd;
    }
}
