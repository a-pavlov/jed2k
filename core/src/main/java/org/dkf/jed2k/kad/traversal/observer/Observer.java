package org.dkf.jed2k.kad.traversal.observer;

import org.dkf.jed2k.Utils;
import org.dkf.jed2k.kad.traversal.algorithm.Traversal;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.kad.KadId;
import org.slf4j.Logger;

/**
 * Created by inkpot on 21.11.2016.
 */
public abstract class Observer {
    public static final byte FLAG_QUERIED = 1;
    public static final byte FLAG_INITIAL = 2;
    public static final byte FLAG_NO_ID = 4;
    public static final byte FLAG_SHORT_TIMEOUT = 8;
    public static final byte FLAG_FAILED = 16;
    public static final byte FLAG_ALIVE = 32;
    public static final byte FLAG_DONE = 64;
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(Observer.class);

    protected final Traversal algorithm;
    protected final Endpoint endpoint;
    protected KadId id;
    // TODO - for future usage
    protected int portTcp;
    protected byte version;
    // end
    protected long sentTime;
    protected int flags = 0;
    protected byte transactionId;

    private boolean wasAbandoned = false;
    private boolean wasSent = false;

    public Observer(final Traversal algorithm
            , final Endpoint ep
            , final KadId id
            , int portTcp
            , byte version
    ) {
        this.algorithm = algorithm;
        this.endpoint = ep;
        this.id = id;
        this.portTcp = portTcp;
        this.version = version;
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

    public abstract void reply(final Serializable s, final Endpoint endpoint);

    /**
     * checks transaction is expected by this observer
     * @param s
     * @return
     */
    public abstract boolean isExpectedTransaction(final Serializable s);

    /**
     * persistent observer is observer which expects more than one response
     * usual situation for search keywords response
     * @return true if observer is persistent
     */
    public boolean expectMultipleResponses() {
        return false;
    }

    public Traversal getAlgorithm() {
        return this.algorithm;
    }

    public Endpoint getEndpoint() {
        return this.endpoint;
    }

    public KadId getId() {
        return this.id;
    }

    public int getPortTcp() {
        return this.portTcp;
    }

    public byte getVersion() {
        return this.version;
    }

    public long getSentTime() {
        return this.sentTime;
    }

    public int getFlags() {
        return this.flags;
    }

    public byte getTransactionId() {
        return this.transactionId;
    }

    public boolean isWasAbandoned() {
        return this.wasAbandoned;
    }

    public boolean isWasSent() {
        return this.wasSent;
    }

    public void setId(KadId id) {
        this.id = id;
    }

    public void setPortTcp(int portTcp) {
        this.portTcp = portTcp;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public void setSentTime(long sentTime) {
        this.sentTime = sentTime;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public void setTransactionId(byte transactionId) {
        this.transactionId = transactionId;
    }

    public void setWasAbandoned(boolean wasAbandoned) {
        this.wasAbandoned = wasAbandoned;
    }

    public void setWasSent(boolean wasSent) {
        this.wasSent = wasSent;
    }

    public String toString() {
        return "Observer(endpoint=" + this.getEndpoint() + ", id=" + this.getId() + ", portTcp=" + this.getPortTcp() + ", version=" + this.getVersion() + ", sentTime=" + this.getSentTime() + ", flags=" + this.getFlags() + ", transactionId=" + this.getTransactionId() + ", wasAbandoned=" + this.isWasAbandoned() + ", wasSent=" + this.isWasSent() + ")";
    }
}
