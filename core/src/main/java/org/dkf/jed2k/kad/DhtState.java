package org.dkf.jed2k.kad;

import org.dkf.jed2k.protocol.Container;
import org.dkf.jed2k.protocol.UInt32;

/**
 * Created by inkpot on 12.12.2016.
 */
public class DhtState implements NodeEntryFun {
    private Container<UInt32, NodeEntry> entries = Container.makeInt(NodeEntry.class);

    @Override
    public void fun(NodeEntry e) {
        entries.add(e);
    }

    public Container<UInt32, NodeEntry> getEntries() {
        return this.entries;
    }
}
