package org.dkf.jed2k.kad.traversal.algorithm;

import org.dkf.jed2k.kad.NodeImpl;
import org.dkf.jed2k.protocol.kad.Kad2Req;
import org.dkf.jed2k.protocol.kad.KadId;

/**
 * Created by inkpot on 07.12.2016.
 */
public class FindKeywords extends FindData {

    public FindKeywords(NodeImpl ni, KadId t) {
        super(ni, t);
    }

    @Override
    protected void update(Kad2Req req) {
        req.setSearchType(FindData.KADEMLIA_FIND_VALUE);
    }
}
