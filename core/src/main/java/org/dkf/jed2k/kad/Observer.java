package org.dkf.jed2k.kad;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.dkf.jed2k.protocol.NetworkIdentifier;
import org.dkf.jed2k.protocol.kad.KadId;
import org.dkf.jed2k.protocol.kad.Transaction;

import java.net.InetSocketAddress;

/**
 * Created by inkpot on 21.11.2016.
 */
@AllArgsConstructor
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
    private InetSocketAddress endpoint;
    private KadId id;
    private byte transactionId;
    private byte flag;


    private boolean wasAbandoned = false;

    public void reply(final Transaction t, final NetworkIdentifier endpoint) {

    }

    public void abort() {

    }

    public void done() {

    }

    public void timeout() {

    }
}
