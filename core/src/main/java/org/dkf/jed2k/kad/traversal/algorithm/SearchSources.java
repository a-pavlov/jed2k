package org.dkf.jed2k.kad.traversal.algorithm;

import org.dkf.jed2k.kad.NodeImpl;
import org.dkf.jed2k.kad.traversal.observer.Observer;
import org.dkf.jed2k.kad.traversal.observer.SearchObserver;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.kad.Kad2SearchSourcesReq;
import org.dkf.jed2k.protocol.kad.KadId;

/**
 * Created by inkpot on 08.12.2016.
 * direct search sources request
 */
public class SearchSources extends Traversal {
    private long size;

    public SearchSources(NodeImpl ni, KadId t, long size) {
        super(ni, t);
        this.size = size;
    }

    @Override
    public Observer newObserver(Endpoint endpoint, KadId id) {
        return new SearchObserver(this, endpoint, id);
    }

    @Override
    public boolean invoke(Observer o) {
        Kad2SearchSourcesReq ssr = new Kad2SearchSourcesReq();
        ssr.setTarget(target);
        ssr.getSize().assign(size);
        ssr.getStartPos().assign(0);
        return nodeImpl.invoke(ssr, o.getEndpoint(), o);
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
