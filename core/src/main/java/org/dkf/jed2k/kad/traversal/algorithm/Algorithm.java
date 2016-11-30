package org.dkf.jed2k.kad.traversal.algorithm;

import org.dkf.jed2k.kad.traversal.observer.Observer;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.kad.KadId;

/**
 * Created by inkpot on 30.11.2016.
 */
public interface Algorithm {

    public void init();

    public void done();

    public boolean invoke(final Observer o);

    public void finished(final Observer o);

    public String getName();

    public void start();

    public void failed(final Observer o, int flags);

    public Observer newObserver(final Endpoint endpoint, final KadId id);

    public void traverse(final Endpoint ep, final KadId id);
}
