package org.dkf.jed2k.kad.traversal;

import org.dkf.jed2k.Time;
import org.dkf.jed2k.kad.Timed;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by inkpot on 24.01.2017.
 */
public class TimedLinkedHashMap<K, V extends Timed> extends LinkedHashMap<K, V> {
    private final long timeout;

    public TimedLinkedHashMap(int initialCapacity, float loadFactor, final long timeout) {
        super(initialCapacity, loadFactor, true);
        this.timeout = timeout;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
        return Time.currentTime() > eldest.getValue().getLastActiveTime() + timeout;
    }
}
