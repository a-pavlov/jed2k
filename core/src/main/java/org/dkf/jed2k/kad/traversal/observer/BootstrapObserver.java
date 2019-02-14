package org.dkf.jed2k.kad.traversal.observer;

import org.dkf.jed2k.kad.traversal.algorithm.Traversal;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.kad.Kad2BootstrapRes;
import org.dkf.jed2k.protocol.kad.KadEntry;
import org.dkf.jed2k.protocol.kad.KadId;
import org.slf4j.Logger;

/**
 * Created by inkpot on 01.12.2016.
 */
public class BootstrapObserver extends Observer {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(BootstrapObserver.class);

    public BootstrapObserver(final Traversal algorithm
            , final Endpoint ep
            , final KadId id
            , int portTcp
            , byte version
    ) {
        super(algorithm, ep, id, portTcp, version);
    }

    @Override
    public void reply(Serializable s, Endpoint endpoint) {
        Kad2BootstrapRes res = (Kad2BootstrapRes)s;
        assert s != null;
        for(KadEntry entry: res.getContacts()) {
            algorithm.traverse(entry.getKadEndpoint().getEndpoint()
                    , entry.getKid()
                    , entry.getKadEndpoint().getPortTcp().intValue()
                    , entry.getVersion());
        }
        done();
    }

    @Override
    public boolean isExpectedTransaction(final Serializable t) {
        return t instanceof Kad2BootstrapRes;
    }
}
