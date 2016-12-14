package org.dkf.jed2k.kad.traversal.observer;

import org.dkf.jed2k.kad.traversal.algorithm.Traversal;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.kad.Kad2Res;
import org.dkf.jed2k.protocol.kad.KadEntry;
import org.dkf.jed2k.protocol.kad.KadId;

/**
 * Created by inkpot on 30.11.2016.
 */
public class FindDataObserver extends Observer {

    public FindDataObserver(final Traversal t
            , final Endpoint ep
            , final KadId id
            , int portTcp
            , byte version) {
        super(t, ep, id, portTcp, version);
    }

    @Override
    public void reply(Serializable s, Endpoint endpoint) {
        Kad2Res res = (Kad2Res)s;
        assert res != null;
        for(final KadEntry entry: res.getResults()) {
            algorithm.traverse(entry.getKadEndpoint().getEndpoint()
                    , entry.getKid()
                    , entry.getKadEndpoint().getPortTcp().intValue()
                    , entry.getVersion());
        }

        done();
    }

    @Override
    public boolean isExpectedTransaction(final Serializable s) {
        return s instanceof Kad2Res;
    }
}
