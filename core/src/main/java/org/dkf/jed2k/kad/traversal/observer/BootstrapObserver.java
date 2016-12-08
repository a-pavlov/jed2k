package org.dkf.jed2k.kad.traversal.observer;

import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.kad.traversal.algorithm.Traversal;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.kad.Kad2BootstrapRes;
import org.dkf.jed2k.protocol.kad.KadEntry;
import org.dkf.jed2k.protocol.kad.KadId;
import org.dkf.jed2k.protocol.kad.Transaction;

/**
 * Created by inkpot on 01.12.2016.
 */
@Slf4j
public class BootstrapObserver extends Observer {

    public BootstrapObserver(final Traversal algorithm, final Endpoint ep, final KadId id) {
        super(algorithm, ep, id);
    }

    @Override
    public void reply(Transaction t, Endpoint endpoint) {
        Kad2BootstrapRes res = (Kad2BootstrapRes)t;
        assert t != null;
        for(KadEntry entry: res.getContacts()) {
            algorithm.traverse(entry.getKadEndpoint().getEndpoint(), entry.getKid());
        }
        done();
    }
}
