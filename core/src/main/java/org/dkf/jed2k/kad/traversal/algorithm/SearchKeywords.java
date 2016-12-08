package org.dkf.jed2k.kad.traversal.algorithm;

import org.dkf.jed2k.kad.NodeImpl;
import org.dkf.jed2k.kad.traversal.observer.Observer;
import org.dkf.jed2k.kad.traversal.observer.SearchObserver;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.kad.Kad2SearchKeysReq;
import org.dkf.jed2k.protocol.kad.KadId;

/**
 * Created by inkpot on 08.12.2016.
 * direct search keywords requests
 */
public class SearchKeywords extends Traversal {

    public SearchKeywords(NodeImpl ni, KadId t) {
        super(ni, t);
    }

    @Override
    public Observer newObserver(Endpoint endpoint, KadId id) {
        return new SearchObserver(this, endpoint, id);
    }

    @Override
    public boolean invoke(Observer o) {
        Kad2SearchKeysReq ssk = new Kad2SearchKeysReq();
        ssk.setTarget(target);
        return nodeImpl.invoke(ssk, o.getEndpoint(), o);
    }

    @Override
    public boolean containsNewNodes() {
        return false;
    }

    @Override
    public void finished(final Observer o) {
        super.finished(o);
        SearchObserver so = (SearchObserver)o;
        assert so != null;
        so.getEntries();
    }
}