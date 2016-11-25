package org.dkf.jed2k.kad;

import lombok.Getter;
import lombok.Setter;
import org.dkf.jed2k.Utils;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.kad.KadId;
import org.dkf.jed2k.protocol.kad.Transaction;

/**
 * Created by inkpot on 21.11.2016.
 */
@Getter
@Setter
public abstract class Observer {
    public static final byte FLAG_QUERIED = 1;
    public static final byte FLAG_INITIAL = 2;
    public static final byte FLAG_NO_ID = 4;
    public static final byte FLAG_SHORT_TIMEOUT = 8;
    public static final byte FLAG_FAILED = 16;
    public static final byte FLAG_ALIVE = 32;
    public static final byte FLAG_DONE = 64;

    private TraversalAlgorithm algorithm;
    private Endpoint endpoint;
    private KadId id;
    private byte transactionId;
    private int flags;

    private boolean wasAbandoned = false;

    public Observer(final TraversalAlgorithm algorithm, final Endpoint ep, final KadId id) {
        this.algorithm = algorithm;
        this.endpoint = ep;
        this.id = id;
    }

    public void shortTimeout() {
        if (Utils.isBit(flags, FLAG_SHORT_TIMEOUT)) return;
        algorithm.failed(this, TraversalAlgorithm.SHORT_TIMEOUT);
    }

    public boolean hasShortTimeout() {
        return Utils.isBit(flags, FLAG_SHORT_TIMEOUT);
    }

    public void timeout() {
        if (Utils.isBit(flags, FLAG_DONE)) return;
        flags |= FLAG_DONE;
        algorithm.failed(this, 0);
    }

    public void abort() {
        if (Utils.isBit(flags, FLAG_DONE)) return;
        flags |= FLAG_DONE;
        algorithm.failed(this, TraversalAlgorithm.PREVENT_REQUEST);
    }

    public void done() {
        if (Utils.isBit(flags, FLAG_DONE)) return;
        flags |= FLAG_DONE;
        algorithm.finished(this);
    }

    public abstract void reply(final Transaction t, final Endpoint endpoint);
}
