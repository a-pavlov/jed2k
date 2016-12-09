package org.dkf.jed2k.kad.traversal.algorithm;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.kad.NodeImpl;
import org.dkf.jed2k.kad.traversal.observer.Observer;
import org.dkf.jed2k.kad.traversal.observer.SearchObserver;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.kad.KadId;
import org.dkf.jed2k.protocol.kad.KadSearchEntry;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by apavlov on 09.12.16.
 * non-reqursive algorithms just to send data to target nodes
 */
public abstract class Direct extends Traversal {
    protected List<KadSearchEntry> accum = new LinkedList<>();

    public Direct(NodeImpl ni, KadId t) throws JED2KException {
        super(ni, t);

    }

    /**
     * no new nodes on direct algorithms
     * @return
     */
    @Override
    public boolean containsNewNodes() {
        return false;
    }

    /**
     * can't add entry to that algorithm at all
     * @param id
     * @param addr
     * @param flags
     */
    @Override
    public void addEntry(final KadId id, final Endpoint addr, byte flags) {
        assert false;
    }

    /**
     * directly add nodes to results list to request them
     * @param ep endpoint ip+port
     * @param id kad id of node
     */
    public void addNode(final Endpoint ep, final KadId id) {
        results.add(newObserver(ep, id));
        numTargetNodes = results.size();    // make sure we will request all results
    }

    @Override
    public void finished(final Observer o) {
        SearchObserver so = (SearchObserver)o;
        assert so != null;

        if (so.getEntries() != null) {
            accum.addAll(so.getEntries());
        }

        super.finished(o);
    }
}
