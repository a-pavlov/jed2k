package org.dkf.jed2k.kad;

import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.kad.traversal.algorithm.Bootstrap;
import org.dkf.jed2k.kad.traversal.algorithm.Traversal;
import org.dkf.jed2k.kad.traversal.observer.NullObserver;
import org.dkf.jed2k.kad.traversal.observer.Observer;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.kad.Kad2Ping;
import org.dkf.jed2k.protocol.kad.KadId;
import org.dkf.jed2k.protocol.kad.Transaction;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by inkpot on 22.11.2016.
 */
@Slf4j
public class NodeImpl {

    private static final int SEARCH_BRANCHING = 5;
    private final RpcManager rpc;
    private DhtTracker tracker = null;
    private RoutingTable table = null;
    private Set<Traversal> runningRequests = new HashSet<>();

    public NodeImpl(final DhtTracker tracker) {
        this.tracker = tracker;
        this.rpc = new RpcManager();
        this.table = new RoutingTable();
    }

    public void addNode(final Endpoint ep, final KadId id) {
        invoke(new Kad2Ping(), ep, new NullObserver(new Traversal(this, id), ep, id));
    }

    public void addTraversalAlgorithm(final Traversal ta) {
        assert !runningRequests.contains(ta);
        runningRequests.add(ta);
    }

    public void removeTraversalAlgorithm(final Traversal ta) {
        assert runningRequests.contains(ta);
        runningRequests.remove(ta);
    }

    public RoutingTable getTable() {
        return table;
    }

    public void tick() {
        rpc.tick();
        KadId target = table.needRefresh();
        if (target != null) refresh(target);
    }

    public void searchSources(final KadId id) {

    }

    public void searchKeywords(final KadId id) {

    }

    // not available now
    public void searchNotes(final KadId id) {

    }

    public void refresh(final KadId id) {

    }

    public void bootstrap(final List<Endpoint> nodes) {
        Traversal t = new Bootstrap(this, null);
        for(Endpoint ep: nodes) {
            t.addEntry(new KadId(), ep, Observer.FLAG_INITIAL);
        }

        t.start();
    }

    public int getSearchBranching() {
        return SEARCH_BRANCHING;
    }

    public void incoming(final Transaction t, final Endpoint ep) {
        Observer o = rpc.incoming(t, ep);

        if (o != null) {
            // if we have endpoint's KAD id in packet - use it
            // else use KAD id from observer
            KadId originId = t.getSelfId().equals(new KadId())?o.getId():t.getSelfId();
            table.nodeSeen(originId, ep);
        }
    }

    void invoke(final Transaction t, final Endpoint ep, final Observer o) {
        try {
            if (tracker.write(t, ep.toInetSocketAddress())) {
                // register transaction if packet was sent
                rpc.invoke(t, ep, o);
            }
        } catch(final JED2KException e) {
            log.error("nodeImpl invoke {} with {} error {}", ep, t, e);
        }
    }
}
