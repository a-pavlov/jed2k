package org.dkf.jed2k.kad.traversal.algorithm;

import org.dkf.jed2k.kad.Listener;
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
    protected Listener sink;

    public Direct(NodeImpl ni, KadId t, final Listener listener) {
        super(ni, t);
        sink = listener;
    }

    /**
     * can't add entry to that algorithm at all
     * @param id
     * @param addr
     * @param flags
     */
    @Override
    public void addEntry(final KadId id, final Endpoint addr, byte flags, int portTcp, byte version) {
        assert false;
    }

    /**
     * directly add nodes to results list to request them
     * @param ep endpoint ip+port
     * @param id kad id of node
     */
    public void addNode(final Endpoint ep, final KadId id, int portTcp, byte version) {
        results.add(newObserver(ep, id, portTcp, version));
        numTargetNodes = results.size();    // make sure we will request all results
    }

    @Override
    public void finished(final Observer o) {
        // TODO - refactor this bad code!
        // avoid search's call specific
        if (o instanceof SearchObserver) {
            SearchObserver so = (SearchObserver) o;
            assert so != null;

            if (so.getEntries() != null) {
                accum.addAll(so.getEntries());
            }

        }

        super.finished(o);
    }

    @Override
    public void done() {
        super.done();
        if (sink != null) sink.process(accum);
    }
}
