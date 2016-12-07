package org.dkf.jed2k.kad.traversal.algorithm;

import org.dkf.jed2k.kad.NodeImpl;
import org.dkf.jed2k.kad.traversal.observer.BootstrapObserver;
import org.dkf.jed2k.kad.traversal.observer.Observer;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.kad.Kad2BootstrapReq;
import org.dkf.jed2k.protocol.kad.KadId;

/**
 * Created by inkpot on 01.12.2016.
 */
public class Bootstrap extends Traversal {

    public Bootstrap(NodeImpl ni, KadId t) {
        super(ni, t);
    }

    @Override
    public Observer newObserver(final Endpoint endpoint, final KadId id) {
        return new BootstrapObserver(this, endpoint, id);
    }

    @Override
    public boolean invoke(final Observer o) {
        Kad2BootstrapReq ping = new Kad2BootstrapReq();
        o.setTransactionId(ping.getTransactionId());
        return nodeImpl.invoke(ping, o.getEndpoint(), o);
    }

    @Override
    public String getName() {
        return "[bootstrap]";
    }
}
