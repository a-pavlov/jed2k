package org.dkf.jed2k.kad.traversal.observer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.Utils;
import org.dkf.jed2k.kad.traversal.algorithm.Traversal;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.kad.KadId;
import org.dkf.jed2k.protocol.kad.Transaction;

/**
 * Created by inkpot on 21.11.2016.
 */
@Getter
@Setter
@ToString(exclude="algorithm")
@Slf4j
public abstract class Observer {
    public static final byte FLAG_QUERIED = 1;
    public static final byte FLAG_INITIAL = 2;
    public static final byte FLAG_NO_ID = 4;
    public static final byte FLAG_SHORT_TIMEOUT = 8;
    public static final byte FLAG_FAILED = 16;
    public static final byte FLAG_ALIVE = 32;
    public static final byte FLAG_DONE = 64;

    protected final Traversal algorithm;
    protected final Endpoint endpoint;
    protected KadId id;
    protected long sentTime;
    protected int flags = 0;
    protected byte transactionId;

    private boolean wasAbandoned = false;
    private boolean wasSent = false;

    public Observer(final Traversal algorithm, final Endpoint ep, final KadId id) {
        this.algorithm = algorithm;
        this.endpoint = ep;
        this.id = id;
        this.sentTime = 0;
    }

    public void shortTimeout() {
        assert !Utils.isBit(flags, FLAG_SHORT_TIMEOUT);
        algorithm.failed(this, Traversal.SHORT_TIMEOUT);
    }

    public boolean hasShortTimeout() {
        return Utils.isBit(flags, FLAG_SHORT_TIMEOUT);
    }

    public void timeout() {
        if (Utils.isBit(flags, FLAG_DONE)) {
            log.debug("[observer] timeout already has DONE flag {}", this);
            return;
        }

        flags |= FLAG_DONE;
        algorithm.failed(this, 0);
    }

    public void abort() {
        if (Utils.isBit(flags, FLAG_DONE)) return;
        flags |= FLAG_DONE;
        algorithm.failed(this, Traversal.PREVENT_REQUEST);
    }

    public void done() {
        log.debug("[observer] done");
        if (Utils.isBit(flags, FLAG_DONE)) {
            log.debug("[observer] done already has DONE flag {}", this);
            return;
        }
        flags |= FLAG_DONE;
        algorithm.finished(this);
    }

    public Endpoint getTarget() {
        return endpoint;
    }

    public String getFlagsStr() {
        return (Utils.isBit(flags, FLAG_QUERIED)?"|FLAG_QUERIED":"")
                +  (Utils.isBit(flags, FLAG_INITIAL)?"|FLAG_INITIAL":"")
                +  (Utils.isBit(flags, FLAG_NO_ID)?"|FLAG_NO_ID":"")
                +  (Utils.isBit(flags, FLAG_SHORT_TIMEOUT)?"|FLAG_SHORT_TIMEOUT":"")
                +  (Utils.isBit(flags, FLAG_FAILED)?"|FLAG_FAILED":"")
                +  (Utils.isBit(flags, FLAG_ALIVE)?"|FLAG_ALIVE":"")
                +  (Utils.isBit(flags, FLAG_DONE)?"|FLAG_DONE":"");
    }

    public abstract void reply(final Transaction t, final Endpoint endpoint);
}
