package org.dkf.jed2k.kad.traversal.algorithm;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.kad.NodeImpl;
import org.dkf.jed2k.protocol.kad.Kad2Req;
import org.dkf.jed2k.protocol.kad.KadId;

/**
 * Created by inkpot on 07.12.2016.
 */
public class FindSources extends FindData {
    private long size;

    public FindSources(NodeImpl ni, KadId t, long size) throws JED2KException {
        super(ni, t);
        this.size = size;
    }

    @Override
    protected void update(Kad2Req req) {
        req.setSearchType(FindData.KADEMLIA_FIND_NODE);
    }

    @Override
    public void done() {
        // process results here start search algorithm
        super.done();
    }
}
