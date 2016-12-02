package org.dkf.jed2k.test.kad;

import org.dkf.jed2k.kad.NodeEntry;
import org.dkf.jed2k.kad.RoutingTable;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.kad.KadId;
import org.junit.Test;

import java.util.Random;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by ap197_000 on 02.12.2016.
 */
public class RoutingTableTest {

    final private KadId target = new KadId(KadId.random(false));

    @Test
    public void bulkTest() {
        Random rnd = new Random();
        RoutingTable table = new RoutingTable(target, 10);
        for(int i = 0; i < 200; ++i) {
            table.addNode(new NodeEntry(new KadId(KadId.random(false)), new Endpoint(rnd.nextInt(), rnd.nextInt(9999)), true));
        }

        assertTrue(table.getBucketsCount() > 0);
        assertEquals(table.getBucketsCount() - 1, table.findBucket(target));
    }
}
