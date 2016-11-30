package org.dkf.jed2k.kad.traversal.algorithm;

import org.dkf.jed2k.kad.traversal.observer.Observer;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.kad.KadId;

/**
 * Created by inkpot on 30.11.2016.
 * this class designed to start search requests to nodes directly
 */
public class Direct implements Algorithm {
    @Override
    public void init() {

    }

    @Override
    public void done() {

    }

    @Override
    public boolean invoke(Observer o) {
        return false;
    }

    @Override
    public void finished(Observer o) {

    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void start() {

    }

    @Override
    public void failed(Observer o, int flags) {

    }

    @Override
    public Observer newObserver(Endpoint endpoint, KadId id) {
        return null;
    }
}
