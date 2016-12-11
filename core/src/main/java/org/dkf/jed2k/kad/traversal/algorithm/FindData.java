package org.dkf.jed2k.kad.traversal.algorithm;

import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.Utils;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.kad.Listener;
import org.dkf.jed2k.kad.NodeEntry;
import org.dkf.jed2k.kad.NodeImpl;
import org.dkf.jed2k.kad.traversal.observer.FindDataObserver;
import org.dkf.jed2k.kad.traversal.observer.Observer;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.kad.Kad2Req;
import org.dkf.jed2k.protocol.kad.KadId;
import org.dkf.jed2k.protocol.kad.ObserverCompareRef;

import java.util.List;

/**
 * Created by inkpot on 07.12.2016.
 */
@Slf4j
public abstract class FindData extends Traversal {

    public static final byte KADEMLIA_FIND_NODE = (byte)0x0b;
    public static final byte KADEMLIA_FIND_VALUE = (byte)0x02;

    protected long size = 0;
    protected final Listener sink;

    public FindData(final NodeImpl ni, final KadId t, long size, final Listener l) {
        super(ni, t);
        this.size = size;
        this.sink = l;
        List<NodeEntry> nodes = ni.getTable().findNode(t, false, 50);
        for(final NodeEntry e: nodes) {
            results.add(newObserver(e.getEndpoint(), e.getId()));
        }

        boolean sorted = Utils.isSorted(results, new ObserverCompareRef(t));

        assert sorted;

        log.debug("[find data] initial size {}", results.size());
    }

    /**
     *
     * @param req request packet
     */
    protected abstract void update(final Kad2Req req);

    protected abstract Direct newTraversal() throws JED2KException;

    @Override
    public Observer newObserver(final Endpoint endpoint, final KadId id) {
        return new FindDataObserver(this, endpoint, id);
    }

    @Override
    public boolean invoke(final Observer o) {
        assert o != null;
        Kad2Req req = new Kad2Req();
        req.setReceiver(o.getId());
        req.setTarget(target);
        o.setTransactionId(req.getTransactionId());
        update(req);
        return nodeImpl.invoke(req, o.getEndpoint(), o);
    }

    @Override
    public void done() {
        nodeImpl.removeTraversalAlgorithm(this);

        try {
            Direct d = newTraversal();
            for(final Observer o: results) {
                // do not request failed nodes now
                if (!Utils.isBit(o.getFlags(), Observer.FLAG_FAILED)) {
                    d.addNode(o.getEndpoint(), o.getId());
                }
            }

            d.start();
            log.debug("[find data] direct search started {}", target);
        } catch(JED2KException e) {
            log.error("[traversal] unable to start search sources algorithm {}", e);
        } finally {
            results.clear();
        }
    }
}
