package org.dkf.jed2k.alert;

import org.dkf.jed2k.protocol.Hash;
import org.dkf.jed2k.protocol.TransferResumeData;

/**
 * Created by ap197_000 on 14.09.2016.
 */
public class TransferResumeDataAlert extends TransferAlert {
    public final TransferResumeData trd;

    public TransferResumeDataAlert(Hash h, final TransferResumeData trd) {
        super(h);
        this.trd = trd;
    }
}
