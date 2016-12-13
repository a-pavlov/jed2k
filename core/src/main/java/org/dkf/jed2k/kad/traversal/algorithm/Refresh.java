package org.dkf.jed2k.kad.traversal.algorithm;

import org.dkf.jed2k.kad.NodeImpl;
import org.dkf.jed2k.kad.traversal.observer.Observer;
import org.dkf.jed2k.kad.traversal.observer.RefreshObserver;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.kad.Kad2HelloReq;
import org.dkf.jed2k.protocol.kad.KadId;
import org.dkf.jed2k.protocol.kad.PacketCombiner;

/**
 * Created by inkpot on 03.12.2016.
 */
public class Refresh extends Traversal {
    public Refresh(NodeImpl ni, KadId t) {
        super(ni, t);
    }

    @Override
    public Observer newObserver(final Endpoint endpoint, final KadId id) {
        return new RefreshObserver(this, endpoint, id);
    }

    @Override
    public boolean invoke(final Observer o) {
        Kad2HelloReq hello = new Kad2HelloReq();
        hello.setKid(nodeImpl.getSelf());
        hello.getPortTcp().assign(nodeImpl.getPort());
        hello.getVersion().assign(PacketCombiner.KADEMLIA_VERSION5_48a);
        return nodeImpl.invoke(hello, o.getEndpoint(), o);
    }

    @Override
    public String getName() {
        return "[refresh]";
    }
}
