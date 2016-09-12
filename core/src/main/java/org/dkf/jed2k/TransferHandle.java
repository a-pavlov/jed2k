package org.dkf.jed2k;

import org.dkf.jed2k.protocol.Hash;
import org.dkf.jed2k.protocol.TransferResumeData;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

/**
 * transfer handle for manipulation of transfer outside of session
 * all manipulations of session data in transfers are synchronized via main session object
 * handle can be invalid if session has no transfer linked with it
 * Created by inkpot on 26.07.2016.
 */
public class TransferHandle implements Comparable<TransferHandle> {
    private WeakReference<Transfer> transfer;
    private Session ses;

    public TransferHandle(final Session s) {
        assert s != null;
        ses = s;
        transfer = new WeakReference<Transfer>(null);
    }

    public TransferHandle(final Session s, Transfer t) {
        assert s != null;
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

        return Hash.INVALID;
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

    public final String getFilepath() {
        Transfer t = transfer.get();
        if (t != null) {
            synchronized (ses) {
                return t.getFilepath();
            }
        }

        return null;
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

    public TransferResumeData getResumeData() {
        Transfer t = transfer.get();
        if (t != null) {
            synchronized (ses) {
                return t.resumeData();
            }
        }

        return null;
    }

    public TransferStatus getStatus() {
        Transfer t = transfer.get();
        if (t != null) {
            synchronized (ses) {
                return t.getStatus();
            }
        }

        return null;
    }

    public List<PeerInfo> getPeersInfo() {
        Transfer t = transfer.get();
        if (t != null) {
            synchronized (ses) {
                return t.getPeersInfo();
            }
        }

        return new LinkedList<PeerInfo>();
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof TransferHandle && ((TransferHandle)o).getHash().equals(getHash()));
    }

    @Override
    public int hashCode() {
        return getHash().hashCode();
    }

    @Override
    public int compareTo(TransferHandle o) {
        return getHash().compareTo(o.getHash());
    }
}
