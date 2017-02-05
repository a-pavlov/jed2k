package org.dkf.jed2k.kad.traversal.observer;

import lombok.Getter;
import org.dkf.jed2k.kad.traversal.algorithm.Traversal;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.kad.Kad2FirewalledReq;
import org.dkf.jed2k.protocol.kad.Kad2FirewalledRes;
import org.dkf.jed2k.protocol.kad.KadId;

/**
 * Created by apavlov on 23.01.17.
 */
@Getter
public class FirewalledObserver extends Observer {
    private int ip;

    public FirewalledObserver(Traversal algorithm, Endpoint ep, KadId id, int portTcp, byte version) {
        super(algorithm, ep, id, portTcp, version);
    }

    @Override
    public void reply(Serializable s, Endpoint endpoint) {
        Kad2FirewalledRes fr = (Kad2FirewalledRes)s;
        ip = fr.getIp();
        done();
    }

    @Override
    public boolean isExpectedTransaction(Serializable s) {
        return (s instanceof Kad2FirewalledRes);
    }
}
