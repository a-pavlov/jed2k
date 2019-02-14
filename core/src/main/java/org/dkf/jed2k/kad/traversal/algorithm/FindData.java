package org.dkf.jed2k.kad.traversal.algorithm;

import org.dkf.jed2k.Utils;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.kad.Filter;
import org.dkf.jed2k.kad.Listener;
import org.dkf.jed2k.kad.NodeEntry;
import org.dkf.jed2k.kad.NodeImpl;
import org.dkf.jed2k.kad.traversal.observer.FindDataObserver;
import org.dkf.jed2k.kad.traversal.observer.Observer;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.kad.Kad2Req;
import org.dkf.jed2k.protocol.kad.KadId;
import org.dkf.jed2k.protocol.kad.ObserverCompareRef;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by inkpot on 07.12.2016.
 */
public abstract class FindData extends Traversal {

    public static final byte KADEMLIA_FIND_NODE = (byte)0x0b;
    public static final byte KADEMLIA_FIND_VALUE = (byte)0x02;
    public static final byte KADEMLIA_STORE = (byte)0x04;
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(FindData.class);

    protected long size = 0;
    protected final Listener sink;

    public FindData(final NodeImpl ni, final KadId t, long size, final Listener l) {
        super(ni, t);
        this.size = size;
        this.sink = l;

        List<NodeEntry> nodes = ni.getTable().forEach(new Filter<NodeEntry>() {
            @Override
            public boolean allow(NodeEntry nodeEntry) {
                return true;
            }
        }, null);

        Collections.sort(nodes, new Comparator<NodeEntry>() {
            @Override
            public int compare(NodeEntry o1, NodeEntry o2) {
                return KadId.compareRef(o1.getId(), o2.getId(), target);
            }
        });

        for(int i = 0; i < Math.min(50, nodes.size()); ++i) {
            final NodeEntry e = nodes.get(i);
            results.add(newObserver(e.getEndpoint(), e.getId(), e.getPortTcp(), e.getVersion()));
        }

        boolean sorted = Utils.isSorted(results, new ObserverCompareRef(t));

        assert sorted;

        log.debug("[find data] for {} initial size {}", getTarget(), results.size());
    }

    /**
     *
     * @param req request packet
     */
    protected abstract void update(final Kad2Req req);

    protected abstract Direct newTraversal() throws JED2KException;

    @Override
    public Observer newObserver(final Endpoint endpoint, final KadId id, int portTcp, byte version) {
        return new FindDataObserver(this, endpoint, id, portTcp, version);
    }

    @Override
    public boolean invoke(final Observer o) {
        assert o != null;
        Kad2Req req = new Kad2Req();
        req.setReceiver(o.getId());
        req.setTarget(target);
        update(req);
        return nodeImpl.invoke(req, o.getEndpoint(), o);
    }

    @Override
    public void done() {
        nodeImpl.removeTraversalAlgorithm(this);

        try {
            Direct d = newTraversal();
            InetSocketAddress sa = nodeImpl.getStoragePoint();

            if (sa != null) {
                Endpoint sp = Endpoint.fromInet(sa);
                log.debug("[find data] add router node to search {}", sp);
                d.addNode(sp, target, sp.getPort(), (byte)0);
            }

            for(final Observer o: results) {
                // do not request failed nodes now
                if (!Utils.isBit(o.getFlags(), Observer.FLAG_FAILED)) {
                    d.addNode(o.getEndpoint(), o.getId(), o.getPortTcp(), o.getVersion());
                }
            }

            d.start();
            log.debug("[find data] direct search started {}", target);
        } catch(JED2KException e) {
            log.error("[find data] unable to start search sources algorithm {}", e);
        } finally {
            results.clear();
        }
    }
}
