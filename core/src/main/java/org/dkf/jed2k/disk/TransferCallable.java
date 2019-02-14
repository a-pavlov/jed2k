package org.dkf.jed2k.disk;

import org.dkf.jed2k.Transfer;

import java.util.concurrent.Callable;

/**
 * Created by apavlov on 29.05.17.
 */
public abstract class TransferCallable<V> implements Callable<V> {
    private final Transfer transfer;

    public TransferCallable(final Transfer t) {
        this.transfer = t;
    }

    public Transfer getTransfer() {
        return this.transfer;
    }
}
