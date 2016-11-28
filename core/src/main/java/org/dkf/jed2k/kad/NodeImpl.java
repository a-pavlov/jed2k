package org.dkf.jed2k.kad;

import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.kad.KadId;

/**
 * Created by inkpot on 22.11.2016.
 */
public class NodeImpl {

    private static final int SEARCH_BRANCHING = 5;
    private final RpcManager rpc;

    public NodeImpl(final RpcManager rpc) {
        this.rpc = rpc;
    }

    public void addNode(final Endpoint ep, final KadId id) {

    }

    public void tick() {

    }

    public void searchSources(final KadId id) {

    }

    public void searchKeywords(final KadId id) {

    }

    // not available now
    public void searchNotes(final KadId id) {

    }

    public int getSearchBranching() {
        return SEARCH_BRANCHING;
    }
}
