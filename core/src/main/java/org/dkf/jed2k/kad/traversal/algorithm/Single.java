package org.dkf.jed2k.kad.traversal.algorithm;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.kad.NodeImpl;
import org.dkf.jed2k.kad.traversal.observer.Observer;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.kad.KadId;

/**
 * Created by inkpot on 30.11.2016.
 * one shot algorithm just to run single observer
 */
public class Single extends Traversal {

    public Single(NodeImpl ni, KadId t) throws JED2KException {
        super(ni, t);
    }

    @Override
    public Observer newObserver(Endpoint endpoint, KadId id) {
        assert false;
        return null;
    }

    @Override
    public boolean invoke(Observer o) {
        assert false;
        return false;
    }
}
