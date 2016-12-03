package org.dkf.jed2k.test.kad;

import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.kad.NodeEntry;
import org.dkf.jed2k.kad.RoutingTable;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.kad.KadId;
import org.junit.Test;

import java.util.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by inkpot on 02.12.2016.
 */
@Slf4j
public class RoutingTableTest {

    final private KadId target = new KadId(KadId.random(false));
    final Random random = new Random();
    final Set<KadId> dict = new HashSet<>();

    KadId getDistinctId() {
        while(true) {
            KadId id = new KadId(KadId.random(false));
            if (dict.contains(id)) {
                continue;
            }

            dict.add(id);
            return id;
        }
    }

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

        // validate each bucket except last contain appropriate nodes
        for(int i = 0; i < table.getBucketsCount() - 1; ++i) {
            RoutingTable.RoutingTableBucket bucket = table.getBucket(i);
            for(final NodeEntry e: bucket.getLiveNodes()) {
                assertEquals(KadId.TOTAL_BITS - 1 - i, KadId.distanceExp(table.getSelf(), e.getId()));
            }

            for(final NodeEntry e: bucket.getReplacements()) {
                assertEquals(KadId.TOTAL_BITS - 1 - i, KadId.distanceExp(table.getSelf(), e.getId()));
            }
        }

        int liveNodesCount = table.getSize().getLeft();
        List<NodeEntry> nodes = table.findNode(table.getSelf(), false, liveNodesCount/2);
        assertEquals(liveNodesCount/2, nodes.size());
    }

    @Test
    public void testExactlyNodes() {
        Random rnd = new Random();
        RoutingTable table = new RoutingTable(target, 10);
        Set<KadId> entries = new HashSet<>();
        for(int i = 0; i < 10; ++i) {
            while(true) {
                KadId id = getDistinctId();
                entries.add(id);
                table.addNode(new NodeEntry(id, new Endpoint(rnd.nextInt(), rnd.nextInt(9999)), true));
                break;
            }
        }

        assertEquals(10, table.getSize().left.intValue());
        assertEquals(1, table.getBucketsCount());

        table.addNode(new NodeEntry(getDistinctId(), new Endpoint(rnd.nextInt(), rnd.nextInt(9999)), false));
        assertEquals(10, table.getSize().left.intValue());
        assertEquals(1, table.getSize().right.intValue());
        for(int i = 0; i < 9; ++i) {
            table.addNode(new NodeEntry(getDistinctId(), new Endpoint(rnd.nextInt(), rnd.nextInt(9999)), false));
        }

        assertEquals(10, table.getSize().left.intValue());
        assertEquals(10, table.getSize().right.intValue());
        assertEquals(1, table.getBucketsCount());

        table.addNode(new NodeEntry(getDistinctId(), new Endpoint(rnd.nextInt(), rnd.nextInt(9999)), false));
        table.addNode(new NodeEntry(getDistinctId(), new Endpoint(rnd.nextInt(), rnd.nextInt(9999)), false));

        assertEquals(10, table.getSize().left.intValue());
        assertEquals(10, table.getSize().right.intValue());
        assertEquals(1, table.getBucketsCount());

        table.addNode(new NodeEntry(getDistinctId(), new Endpoint(rnd.nextInt(), rnd.nextInt(9999)), true));
        assertEquals(2, table.getBucketsCount());
        assertTrue(table.getSize().left.intValue() > 10);
        assertTrue(table.getSize().left.intValue() <= 20);
        assertTrue(table.getSize().right.intValue() >= 0);
        assertTrue(table.getSize().right.intValue() < 10);
    }

    @Test
    public void testFindNodeAndTimeout() throws JED2KException {
        RoutingTable table = new RoutingTable(target, 4);
        NodeEntry entry = new NodeEntry(getDistinctId(), Endpoint.fromString("88.22.0.33", 1020), true);
        table.addNode(entry);
        List<NodeEntry> entries = table.findNode(target, false, 1);
        assertEquals(1, entries.size());
        table.nodeFailed(entry.getId(), entry.getEndpoint());
        table.nodeFailed(entry.getId(), entry.getEndpoint());
        table.nodeFailed(entry.getId(), entry.getEndpoint());
        List<NodeEntry> entries2 = table.findNode(target, true, 1);
        assertTrue(table.findNode(target, false, 1).isEmpty());
        assertEquals(1, entries2.size());
    }
}
