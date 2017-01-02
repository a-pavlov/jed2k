package org.dkf.jed2k.kad.traversal.observer;

import lombok.Getter;
import org.dkf.jed2k.kad.traversal.algorithm.Traversal;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.kad.Kad2SearchRes;
import org.dkf.jed2k.protocol.kad.KadId;
import org.dkf.jed2k.protocol.kad.KadSearchEntry;

import java.util.List;

/**
 * Created by inkpot on 08.12.2016.
 */
@Getter
public class SearchObserver extends Observer {
    private List<KadSearchEntry> entries = null;

    public SearchObserver(Traversal algorithm
            , Endpoint ep
            , KadId id
            , int portTcp
            , byte version) {
        super(algorithm, ep, id, portTcp, version);
    }

    @Override
    public void reply(Serializable s, Endpoint endpoint) {
        Kad2SearchRes res = (Kad2SearchRes)s;
        assert res != null;
        entries = res.getResults().getList();
        done();
    }

    @Override
    public boolean isExpectedTransaction(final Serializable s) {
        if (s instanceof Kad2SearchRes) {
            /**
             * response contains target hash - check it here to choose correct observer
             */
            return algorithm.getTarget().compareTo(((Kad2SearchRes)s).getTarget()) == 0;
        }

        return false;
    }
}
