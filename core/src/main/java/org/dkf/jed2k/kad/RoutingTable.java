package org.dkf.jed2k.kad;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.Checker;
import org.dkf.jed2k.Pair;
import org.dkf.jed2k.Time;
import org.dkf.jed2k.Utils;
import org.dkf.jed2k.protocol.NetworkIdentifier;
import org.dkf.jed2k.protocol.kad.KadId;

import java.util.*;

/**
 * Created by inkpot on 23.11.2016.
 */
@Slf4j
public class RoutingTable {

    @Data
    @EqualsAndHashCode
    private static class RoutingTableBucket {
        private ArrayList<NodeEntry> replacements = new ArrayList<>();
        private ArrayList<NodeEntry> liveNodes = new ArrayList<>();
        private long lastActive = Time.currentTime();

        void removeEntry(final NodeEntry e) {
            replacements.remove(e);
            liveNodes.remove(e);
        }
    }

    private ArrayList<RoutingTableBucket> buckets = new ArrayList<>();
    private KadId self;

    // the last time need_bootstrap() returned true
    private long lastBootstrap;

    // the last time the routing table was refreshed.
    // this is used to stagger buckets needing refresh
    // to be at least 45 seconds apart.
    private long lastRefresh;

    // the last time we refreshed our own bucket
    // refreshed every 15 minutes
    private long lastSelfRefresh;

    // this is a set of all the endpoints that have
    // been identified as router nodes. They will
    // be used in searches, but they will never
    // be added to the routing table.
    Set<NetworkIdentifier> routerNodes = new HashSet<>();

    // these are all the IPs that are in the routing
    // table. It's used to only allow a single entry
    // per IP in the whole table. Currently only for
    // IPv4
    //MultiSemultiset<address_v4::bytes_type> m_ips;
    Set<Integer> ips = new HashSet<>();

    private int bucketSize;

    public Pair<Integer, Integer> size() {
        int nodes = 0;
        int replacements = 0;
        for (final RoutingTableBucket node: buckets) {
            nodes += node.getLiveNodes().size();
            replacements += node.getReplacements().size();
        }

        return Pair.make(nodes, replacements);
    }

    public int numGlobalNodes() {
        int deepestBucket = 0;
        int deepestSize = 0;
        for (final RoutingTableBucket node: buckets) {
            deepestSize = node.getLiveNodes().size(); // + i->replacements.size();
            if (deepestSize < bucketSize) break;
            // this bucket is full
            ++deepestBucket;
        }

        if (deepestBucket == 0) return 1 + deepestSize;

        if (deepestSize < bucketSize / 2) return (1 << deepestBucket) * bucketSize;
        else return (2 << deepestBucket) * deepestSize;
    }

    public void touchBucket(final KadId target) {
        RoutingTableBucket bucket = findBucket(target);
        bucket.setLastActive(Time.currentTime());
    }

    public RoutingTableBucket findBucket(final KadId id) {

        int numBuckets = buckets.size();
        if (numBuckets == 0) {
            buckets.add(new RoutingTableBucket());
            // add 160 seconds to prioritize higher buckets (i.e. buckets closer to us)
            buckets.get(buckets.size() - 1).setLastActive(Time.currentTime() + Time.seconds(160));
            ++numBuckets;
        }

        int bucket_index = Math.min(KadId.TOTAL_BITS - 1 - KadId.distanceExp(self, id), numBuckets - 1);
        assert (bucket_index < buckets.size());
        assert (bucket_index >= 0);

        return buckets.get(bucket_index);
    }

    KadId needRefresh() {
        long now = Time.currentTime();

        // refresh our own bucket once every 15 minutes
        if (now - lastSelfRefresh > Time.minutes(15)) {
            lastSelfRefresh = now;
            log.debug("table need_refresh [ bucket: self target: {}]", self);
            return self;
        }

        if (buckets.isEmpty()) return null;

        RoutingTableBucket bucket = Collections.min(buckets, new Comparator<RoutingTableBucket>() {
            @Override
            public int compare(RoutingTableBucket lhs, RoutingTableBucket rhs) {
                // add the number of nodes to prioritize buckets with few nodes in them
                long diff = lhs.getLastActive() + Time.seconds(lhs.getLiveNodes().size() * 5) -
                        rhs.getLastActive() + Time.seconds(rhs.getLiveNodes().size() * 5);
                if (diff < 0) return -1;
                if (diff > 0) return 1;
                return 0;
            }
        });

        assert bucket != null;

        int i = buckets.indexOf(bucket);

        assert i >= 0;
        assert i < buckets.size();

        if (now - bucket.getLastActive() < Time.minutes(15)) return null;
        if (now - lastRefresh < Time.seconds(45)) return null;

        // generate a random node_id within the given bucket
        KadId target = new KadId(KadId.random(false));
        int num_bits = i + 1;  // std::distance(begin, itr) + 1 in C++
        KadId mask = new KadId();

        for (int j = 0; j < num_bits; ++j) {
            mask.set(j/8, (byte)(mask.at(j/8) | (byte)(0x80 >> (j&7))));
        };

        // target = (target & ~mask) | (root & mask)
        KadId root = new KadId(self);
        root.bitsAnd(mask);
        target.bitsAnd(mask.bitsInverse());
        target.bitsOr(root);

        // make sure this is in another subtree than m_id
        // clear the (num_bits - 1) bit and then set it to the
        // inverse of m_id's corresponding bit.
        int bitPos = (num_bits - 1) / 8;
        target.set(bitPos, (byte)(target.at(bitPos) & (byte)(~(0x80 >> ((num_bits - 1) % 8)))));
        target.set(bitPos, (byte)(target.at(bitPos) | (byte)((~(self.at(bitPos))) & (byte)(0x80 >> ((num_bits - 1) % 8)))));

        assert KadId.distanceExp(self, target) == KadId.TOTAL_BITS - num_bits;

        log.debug("table need_refresh [ bucket: {} target: {} ]", num_bits, target);
        lastRefresh = now;
        return target;
    }

    Pair<NodeEntry, RoutingTableBucket> findNode(final NetworkIdentifier ep) {
        for (RoutingTableBucket bucket: buckets) {
            for (NodeEntry n: bucket.getReplacements()) {
                if (!n.getEndpoint().equals(ep)) continue;
                return Pair.make(n, bucket);
            }

            for (NodeEntry n: bucket.getLiveNodes()) {
                if (!n.getEndpoint().equals(ep)) continue;
                return Pair.make(n, bucket);
            }
        }

        return null;
    }



    @AllArgsConstructor
    private static class FindByKadId implements Checker<NodeEntry> {
        private final KadId target;

        @Override
        public boolean check(NodeEntry nodeEntry) {
            return false;
        }
    }

    public void nodeFailed(final KadId id, final NetworkIdentifier ep) {
        // if messages to ourself fails, ignore it
        if (id.equals(self)) return;

        RoutingTableBucket bucket = findBucket(id);

        assert bucket != null;
        int j = Utils.indexOf(bucket.getLiveNodes(), new FindByKadId(id));

        if (j == -1) return;

        // if the endpoint doesn't match, it's a different node
        // claiming the same ID. The node we have in our routing
        // table is not necessarily stale
        NodeEntry failedNode = bucket.getLiveNodes().get(j);
        if (!failedNode.getEndpoint().equals(ep)) return;

        if (bucket.getReplacements().isEmpty()) {
            failedNode.timedOut();
            log.debug("table NODE FAILED id: {} ip: {} fails: {} pinged: {} uptime: {}"
                    , id
                    , failedNode.failCount()
                    , failedNode.isPinged()
                    , Time.currentTime() - failedNode.getFirstSeen());

            // if this node has failed too many times, or if this node
            // has never responded at all, remove it
            if (failedNode.failCount() >= 10 || !failedNode.isPinged()) {
                ips.remove(failedNode.getEndpoint().getIP());
                bucket.getLiveNodes().remove(j);
            }

            return;
        }

        ips.remove(failedNode.getEndpoint().getIP());
        bucket.getLiveNodes().remove(j);

        j = Utils.indexOf(bucket.getReplacements(), new Checker<NodeEntry>() {
            @Override
            public boolean check(NodeEntry nodeEntry) {
                return nodeEntry.isPinged();
            }
        });

        if (j == -1) j = 0;
        bucket.getLiveNodes().add(bucket.getReplacements().get(j));
        bucket.getReplacements().remove(j);
    }

    boolean needBootstrap() {
        if (Time.currentTime() - lastBootstrap < Time.seconds(30)) return false;

        for (RoutingTableBucket bucket: buckets) {
            for (final NodeEntry node: bucket.getLiveNodes()) {
                if (node.isConfirmed()) return false;
            }
        }

        lastBootstrap = Time.currentTime();
        return true;
    }
}
