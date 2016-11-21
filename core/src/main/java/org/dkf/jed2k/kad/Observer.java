package org.dkf.jed2k.kad;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.dkf.jed2k.protocol.NetworkIdentifier;
import org.dkf.jed2k.protocol.kad.KadId;
import org.dkf.jed2k.protocol.kad.TransactionIdentifier;

import java.net.InetSocketAddress;

/**
 * Created by inkpot on 21.11.2016.
 */
@AllArgsConstructor
@Getter
@Setter
public abstract class Observer {
    public final byte QUERIED = 1;
    public final byte INITIAL = 2;
    public final byte NO_ID = 4;
    public final byte SHORT_TIMEOUT = 8;
    public final byte FAILED = 16;
    public final byte ALIVE = 32;
    public final byte DONE = 64;

    private TraversalAlgorithm algorithm;
    private InetSocketAddress endpoint;
    private KadId id;
    private byte transactionId;
    private byte flag;

    public void reply(final TransactionIdentifier t, final NetworkIdentifier endpoint) {

    }

    public void abort() {

    }

    public void done() {

    }

    public void timeout() {

    }
}
