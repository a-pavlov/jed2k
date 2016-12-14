package org.dkf.jed2k.kad.traversal.observer;

import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.kad.traversal.algorithm.Traversal;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.kad.KadId;

/**
 * Created by inkpot on 28.11.2016.
 */
@Slf4j
public class NullObserver extends Observer {

    public NullObserver(final Traversal algorithm
            , final Endpoint ep
            , final KadId id
            , int portTcp
            , byte version) {
        super(algorithm, ep, id, portTcp, version);
    }

    @Override
    public void reply(Serializable s, Endpoint endpoint) {
        log.trace("[null] reply");
        flags |= Observer.FLAG_DONE;
    }

    @Override
    public boolean isExpectedTransaction(final Serializable s) {
        return true;
    }
}
