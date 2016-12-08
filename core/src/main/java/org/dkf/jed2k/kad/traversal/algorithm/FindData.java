package org.dkf.jed2k.kad.traversal.algorithm;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.kad.NodeImpl;
import org.dkf.jed2k.kad.traversal.observer.FindDataObserver;
import org.dkf.jed2k.kad.traversal.observer.Observer;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.kad.Kad2Req;
import org.dkf.jed2k.protocol.kad.KadId;

/**
 * Created by inkpot on 07.12.2016.
 */
public abstract class FindData extends Traversal {

    public static final byte KADEMLIA_FIND_NODE = (byte)0x0b;
    public static final byte KADEMLIA_FIND_VALUE = (byte)0x02;

    public FindData(NodeImpl ni, KadId t) throws JED2KException {
        super(ni, t);
    }

    /**
     *
     * @param req request packet
     */
    protected abstract void update(final Kad2Req req);

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
}
