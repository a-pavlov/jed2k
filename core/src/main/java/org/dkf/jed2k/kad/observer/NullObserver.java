package org.dkf.jed2k.kad.observer;

import org.dkf.jed2k.kad.Observer;
import org.dkf.jed2k.kad.TraversalAlgorithm;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.kad.KadId;
import org.dkf.jed2k.protocol.kad.Transaction;

/**
 * Created by inkpot on 28.11.2016.
 */
public class NullObserver extends Observer {

    public NullObserver(final TraversalAlgorithm algorithm, final Endpoint ep, final KadId id) {
        super(algorithm, ep, id);
    }

    @Override
    public void reply(Transaction t, Endpoint endpoint) {
        flags |= FLAG_DONE;
    }
}
