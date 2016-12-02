package org.dkf.jed2k.test.kad;

import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.kad.NodeEntry;
import org.dkf.jed2k.kad.RoutingTable;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.kad.KadId;
import org.junit.Test;

import java.util.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by ap197_000 on 02.12.2016.
 */
@Slf4j
public class RoutingTableTest {

    final private KadId target = new KadId(KadId.random(false));

    @Test
    public void bulkTest() {
        Random rnd = new Random();
        RoutingTable table = new RoutingTable(target, 10);
        Set<NodeEntry> aliveNodes = new HashSet<>();
        List<NodeEntry> allNodes = new ArrayList<>();
        for(int i = 0; i < 200; ++i) {
            NodeEntry entry = new NodeEntry(new KadId(KadId.random(false)), new Endpoint(rnd.nextInt(), rnd.nextInt(9999)), rnd.nextBoolean());
            table.addNode(entry);
            allNodes.add(entry);
            if (entry.isPinged()) aliveNodes.add(entry);
            if ((i + 1) % 10 == 0) {
                for(int j = 0; j < 3; ++j) {
                    NodeEntry e = allNodes.get(rnd.nextInt(allNodes.size() - 1));
                    table.nodeFailed(e.getId(), e.getEndpoint());
                }
            }

        }

        assertTrue(table.getBucketsCount() > 0);
        assertEquals(table.getBucketsCount() - 1, table.findBucket(target));
        log.info("table alive nodes {}", aliveNodes.size());
        log.info("table buckets count {} total nodes {}", table.getBucketsCount(), table.getSize());
    }
}
