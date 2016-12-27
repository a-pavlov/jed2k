package org.dkf.jed2k.kad.traversal.observer;

import org.dkf.jed2k.kad.traversal.algorithm.Traversal;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.kad.Kad2Pong;
import org.dkf.jed2k.protocol.kad.KadId;

/**
 * Created by inkpot on 01.12.2016.
 */
public class RefreshObserver extends Observer {

    public RefreshObserver(Traversal algorithm
            , Endpoint ep
            , KadId id
            , int portTcp
            , byte version) {
        super(algorithm, ep, id, portTcp, version);
    }

    @Override
    public void reply(Serializable s, Endpoint endpoint) {
        Kad2Pong pong = (Kad2Pong) s;
        assert pong != null;
        done();
    }

    @Override
    public boolean isExpectedTransaction(final Serializable s) {
        return s instanceof Kad2Pong;
    }
}
