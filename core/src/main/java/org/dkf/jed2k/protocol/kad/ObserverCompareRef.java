package org.dkf.jed2k.protocol.kad;

import org.dkf.jed2k.kad.traversal.observer.Observer;

import java.util.Comparator;

/**
 * Created by inkpot on 11.12.2016.
 */
public class ObserverCompareRef implements Comparator<Observer> {
    private final KadId target;

    public ObserverCompareRef(final KadId target) {
        this.target = target;
    }

    @Override
    public int compare(Observer o1, Observer o2) {
        return KadId.compareRef(o1.getId(), o2.getId(), target);
    }
}
