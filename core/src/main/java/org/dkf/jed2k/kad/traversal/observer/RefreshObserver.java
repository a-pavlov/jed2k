package org.dkf.jed2k.kad.traversal.observer;

import org.dkf.jed2k.kad.traversal.algorithm.Algorithm;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.kad.Kad2HelloRes;
import org.dkf.jed2k.protocol.kad.KadId;
import org.dkf.jed2k.protocol.kad.Transaction;

/**
 * Created by inkpot on 01.12.2016.
 */
public class RefreshObserver extends Observer {

    public RefreshObserver(Algorithm algorithm, Endpoint ep, KadId id) {
        super(algorithm, ep, id);
    }

    @Override
    public void reply(Transaction t, Endpoint endpoint) {
        Kad2HelloRes res = (Kad2HelloRes)t;
        assert res != null;
        done();
    }
}
