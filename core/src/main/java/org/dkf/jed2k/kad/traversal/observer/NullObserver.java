package org.dkf.jed2k.kad.traversal.observer;

import org.dkf.jed2k.kad.traversal.algorithm.Traversal;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.kad.KadId;
import org.dkf.jed2k.protocol.kad.Transaction;

/**
 * Created by inkpot on 28.11.2016.
 */
public class NullObserver extends Observer {

    public NullObserver(final Traversal algorithm, final Endpoint ep, final KadId id) {
        super(algorithm, ep, id);
    }

    @Override
    public void reply(Transaction t, Endpoint endpoint) {
        done();
    }

    @Override
    public boolean isExpectedTransaction(Transaction t) {
        return true;
    }
}
