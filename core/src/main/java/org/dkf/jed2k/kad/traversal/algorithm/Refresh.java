package org.dkf.jed2k.kad.traversal.algorithm;

import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.kad.NodeImpl;
import org.dkf.jed2k.protocol.kad.Kad2Req;
import org.dkf.jed2k.protocol.kad.KadId;

/**
 * Created by inkpot on 03.12.2016.
 */
@Slf4j
public class Refresh extends FindData {

    public Refresh(NodeImpl ni, KadId t) {
        super(ni, t, 0, null);
    }

    @Override
    protected void update(Kad2Req req) {
        req.setSearchType(FindData.KADEMLIA_FIND_NODE);
    }

    @Override
    protected Direct newTraversal() throws JED2KException {
        return null;
    }

    @Override
    public String getName() {
        return "[refresh]";
    }

    @Override
    public void done() {
        // just remove algorithm from node
        log.debug("[refresh] done");
        nodeImpl.removeTraversalAlgorithm(this);
    }
}
