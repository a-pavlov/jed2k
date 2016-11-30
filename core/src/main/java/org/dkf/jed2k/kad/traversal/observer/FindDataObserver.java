package org.dkf.jed2k.kad.traversal.observer;

import org.dkf.jed2k.kad.traversal.algorithm.Algorithm;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.kad.Kad2Res;
import org.dkf.jed2k.protocol.kad.KadEntry;
import org.dkf.jed2k.protocol.kad.KadId;
import org.dkf.jed2k.protocol.kad.Transaction;

/**
 * Created by inkpot on 30.11.2016.
 */
public class FindDataObserver extends Observer {

    public FindDataObserver(final Algorithm t, final Endpoint ep, final KadId id) {
        super(t, ep, id);
    }

    @Override
    public void reply(Transaction t, Endpoint endpoint) {
        Kad2Res res = (Kad2Res)t;
        assert res != null;
        for(final KadEntry entry: res.getResults()) {
            algorithm.traverse(entry.getKadEndpoint().getEndpoint(), entry.getKid());
        }

        done();
    }
}
