package org.jed2k;

import org.jed2k.protocol.Hash;

import java.lang.ref.WeakReference;

/**
 * transfer handle for manipulation of transfer outside of session
 * all manipulations of session data in transfers are synchronized via main session object
 * handle can be invalid if session has no transfer linked with it
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
                t.pause();
            }
        }
    }

    public final void resume() {
        Transfer t = transfer.get();
        if (t != null) {
            synchronized (ses) {
                t.resume();
            }
        }
    }

    public final boolean isPaused() {
        boolean res = false;
        Transfer t = transfer.get();
        if (t != null) {
            synchronized (ses) {
                res = t.isPaused();
            }
        }
        return res;
    }

    public final boolean isResumed() {
        boolean res = false;
        Transfer t = transfer.get();
        if (t != null) {
            synchronized (ses) {
                res = !t.isPaused();
            }
        }
        return res;
    }
}
