package org.dkf.jed2k.protocol.kad;

import java.util.Comparator;

/**
 * Created by inkpot on 11.12.2016.
 */
public class CompareRef implements Comparator<KadId> {
    private final KadId target;

    public CompareRef(final KadId target) {
        this.target = target;
    }

    @Override
    public int compare(KadId o1, KadId o2) {
        return KadId.compareRef(o1, o2, target);
    }
}
