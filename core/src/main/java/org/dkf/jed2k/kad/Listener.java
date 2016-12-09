package org.dkf.jed2k.kad;

import org.dkf.jed2k.protocol.kad.KadSearchEntry;

import java.util.List;

/**
 * Created by apavlov on 09.12.16.
 */
public interface Listener {
    public void process(final List<KadSearchEntry> data);
}
