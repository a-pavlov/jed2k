package org.jed2k;

import org.jed2k.protocol.Hash;

import java.lang.ref.WeakReference;

/**
 * transfer handle for manipulation of transfer outside of session
 * Created by inkpot on 26.07.2016.
 */
public class TransferHandle {
    private WeakReference<Transfer> transfer;
    private Session ses;

    public TransferHandle(final Session s) {
        ses = s;
        transfer = new WeakReference<Transfer>(null);
    }

    public TransferHandle(final Session s, Transfer t) {
        ses = s;
        transfer = new WeakReference<Transfer>(t);
    }

    public final boolean isValid() {
        return transfer.get() != null;
    }

    public final Hash getHash() {
        Transfer t = transfer.get();
        if (t != null) {
            synchronized (ses) {
                return t.hash();
            }
        }

        return null;
    }

    public final long getSize() {
        return 0;
    }

    public final String getName() {
        return "";
    }

    public final String getPath() {
        return "";
    }

    public final void pause() {

    }

    public final void resume() {

    }
}
