package org.dkf.jed2k.kad.traversal.algorithm;

import org.dkf.jed2k.Utils;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.kad.NodeImpl;
import org.dkf.jed2k.kad.traversal.observer.BootstrapObserver;
import org.dkf.jed2k.kad.traversal.observer.Observer;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.kad.Kad2BootstrapReq;
import org.dkf.jed2k.protocol.kad.KadId;
import org.slf4j.Logger;

/**
 * Created by inkpot on 01.12.2016.
 */
public class Bootstrap extends Traversal {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(Bootstrap.class);

    public Bootstrap(NodeImpl ni, KadId t) {
        super(ni, t);
    }

    @Override
    public Observer newObserver(final Endpoint endpoint, final KadId id, int portTcp, byte version) {
        return new BootstrapObserver(this, endpoint, id, portTcp, version);
    }

    @Override
    public boolean invoke(final Observer o) {
        Kad2BootstrapReq ping = new Kad2BootstrapReq();
        return nodeImpl.invoke(ping, o.getEndpoint(), o);
    }

    @Override
    public String getName() {
        return "[bootstrap]";
    }

    @Override
    public void done() {
        for(final Observer o: results) {
            if (Utils.isBit(o.getFlags(), Observer.FLAG_QUERIED)) continue;
            try {
                nodeImpl.addNode(o.getEndpoint(), o.getId());
            } catch(JED2KException e) {
                log.error("[bootstrap] ping {} failed with {}", o, e);
            }
        }

        super.done();
    }
}
