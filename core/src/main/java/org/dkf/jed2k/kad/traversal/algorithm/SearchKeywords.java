package org.dkf.jed2k.kad.traversal.algorithm;

import org.dkf.jed2k.kad.Listener;
import org.dkf.jed2k.kad.NodeImpl;
import org.dkf.jed2k.kad.traversal.observer.Observer;
import org.dkf.jed2k.kad.traversal.observer.SearchObserver;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.kad.Kad2PublishKeysReq;
import org.dkf.jed2k.protocol.kad.Kad2SearchKeysReq;
import org.dkf.jed2k.protocol.kad.KadId;
import org.dkf.jed2k.protocol.kad.KadSearchEntry;
import org.dkf.jed2k.protocol.tag.Tag;

/**
 * Created by inkpot on 08.12.2016.
 * direct search keywords requests
 */
public class SearchKeywords extends Direct {

    public SearchKeywords(final NodeImpl ni, final KadId t, final Listener listener) {
        super(ni, t, listener);
    }

    @Override
    public Observer newObserver(Endpoint endpoint, KadId id, int portTcp, byte version) {
        return new SearchObserver(this, endpoint, id, portTcp, version);
    }

    @Override
    public boolean invoke(Observer o) {
        Kad2SearchKeysReq ssk = new Kad2SearchKeysReq();
        ssk.setTarget(target);
        return nodeImpl.invoke(ssk, o.getEndpoint(), o);
    }

    @Override
    public void writeFailedObserverToRoutingTable(final Observer o) {
        // do nothing since peer possibly has no information for us
    }

    @Override
    public void finished(final Observer o) {
        SearchObserver so = (SearchObserver) o;

        // generate publish sources request from search result
        if (so.getEntries() != null) {
            Kad2PublishKeysReq res = new Kad2PublishKeysReq();
            res.setKeywordId(o.getId());
            for(final KadSearchEntry kse: so.getEntries()) {
                // inject endpoint into search result entry - it will be used as source host in KAD storage
                kse.getInfo().add(Tag.tag(Tag.TAG_SOURCETYPE, null, o.getEndpoint().getIP()));
                res.getSources().add(kse);
            }

            nodeImpl.process(res, null);
        }

        super.finished(o);
    }
}