package org.dkf.jed2k.kad;

import org.dkf.jed2k.protocol.kad.KadId;
import org.dkf.jed2k.protocol.kad.TransactionIdentifier;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by inkpot on 21.11.2016.
 */
public class RpcManager {
    private List<Observer> transactions = new LinkedList<>();

    public void invoke(final TransactionIdentifier t, final InetSocketAddress ep, final Observer o) {

    }

    public void incoming(final TransactionIdentifier t, final InetSocketAddress ep, final KadId id) {

    }

    public void unreachable(final InetSocketAddress ep) {

    }

    public void abort() {

    }

    public void tick() {

    }
}
