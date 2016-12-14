package org.dkf.jed2k.kad.traversal.algorithm;

import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.Utils;
import org.dkf.jed2k.kad.NodeEntry;
import org.dkf.jed2k.kad.NodeImpl;
import org.dkf.jed2k.kad.traversal.observer.Observer;
import org.dkf.jed2k.kad.traversal.observer.RefreshObserver;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.kad.Kad2Ping;
import org.dkf.jed2k.protocol.kad.KadId;
import org.dkf.jed2k.protocol.kad.ObserverCompareRef;

import java.util.List;

/**
 * Created by inkpot on 03.12.2016.
 */
@Slf4j
public class Refresh extends Traversal {
    public Refresh(NodeImpl ni, KadId t) {
        super(ni, t);

        List<NodeEntry> nodes = ni.getTable().findNode(t, false, 50);
        for(final NodeEntry e: nodes) {
            results.add(newObserver(e.getEndpoint(), e.getId()));
        }
        boolean sorted = Utils.isSorted(results, new ObserverCompareRef(t));
        assert sorted;
        log.debug("[refresh] initial size is {}", results.size());
    }

    @Override
    public Observer newObserver(final Endpoint endpoint, final KadId id) {
        return new RefreshObserver(this, endpoint, id);
    }

    @Override
    public boolean invoke(final Observer o) {
        return nodeImpl.invoke(new Kad2Ping(), o.getEndpoint(), o);
    }

    @Override
    public String getName() {
        return "[refresh]";
    }
}
