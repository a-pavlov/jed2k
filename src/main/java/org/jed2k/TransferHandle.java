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
        Transfer t = transfer.get();
        if (t != null) {
            synchronized (ses) {
                return t.size();
            }
        }

        return 0;
    }

    public final String getName() {
        return "";
    }

    public final String getPath() {
        return "";
    }

    public final void pause() {
        Transfer t = transfer.get();
        if (t != null) {
            synchronized (ses) {
                // execute pause on transfer
            }
        }
    }

    public final void resume() {
        Transfer t = transfer.get();
        if (t != null) {
            synchronized (ses) {
                // execute resume on transfer
            }
        }
    }

    public final boolean isPaused() {
        boolean res = false;
        Transfer t = transfer.get();
        if (t != null) {
            synchronized (ses) {
                res = false;
            }
        }
        return res;
    }

    public final boolean isResumed() {
        boolean res = false;
        Transfer t = transfer.get();
        if (t != null) {
            synchronized (ses) {
                // extract actual transfer state
            }
        }
        return res;
    }
}
