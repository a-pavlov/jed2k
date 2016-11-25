package org.dkf.jed2k.kad;

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
        buckets.get(findBucket(target)).setLastActive(Time.currentTime());
    }

    public int findBucket(final KadId id) {

        int numBuckets = buckets.size();
        if (numBuckets == 0) {
            buckets.add(new RoutingTableBucket());
            // add 160 seconds to prioritize higher buckets (i.e. buckets closer to us)
            buckets.get(buckets.size() - 1).setLastActive(Time.currentTime() + Time.seconds(160));
            ++numBuckets;
        }

        int bucketIndex = Math.min(KadId.TOTAL_BITS - 1 - KadId.distanceExp(self, id), numBuckets - 1);
        assert (bucketIndex < buckets.size());
        assert (bucketIndex >= 0);
        return bucketIndex;
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

    private static class FindByKadId implements Checker<NodeEntry> {
        private final KadId target;

        public FindByKadId(final KadId id) {
            target = id;
        }

        @Override
        public boolean check(NodeEntry nodeEntry) {
            return false;
        }
    }

    public boolean addNode(final NodeEntry e) {
        if (routerNodes.contains(e.getEndpoint())) return false;

        boolean ret = needBootstrap();

        // don't add ourself
        if (e.getId().equals(self)) return ret;

        // do we already have this IP in the table?
        if (ips.contains(e.getEndpoint().getIP())) {
            // this exact IP already exists in the table. It might be the case
            // that the node changed IP. If pinged is true, and the port also
            // matches then we assume it's in fact the same node, and just update
            // the routing table
            // pinged means that we have sent a message to the IP, port and received
            // a response with a correct transaction ID, i.e. it is verified to not
            // be the result of a poisoned routing table

            Pair<NodeEntry, RoutingTableBucket> existing = findNode(e.getEndpoint());
            if (!e.isPinged() || existing == null) {
                // the new node is not pinged, or it's not an existing node
                // we should ignore it, unless we allow duplicate IPs in our
                // routing table
                //if (m_settings.restrict_routing_ips) {
                //    log.debug("table ignoring node (duplicate IP): {} {}", e.getId(), e.getEndpoint());
                //    return ret;
                //}
            }
            else if (existing != null && existing.left.getId().equals(e.getId())) {
                // if the node ID is the same, just update the failcount
                // and be done with it
                existing.left.setTimeoutCount(0);
                return ret;
            }
            else if (existing != null) {
                assert !existing.left.getId().equals(e.getId());
                // this is the same IP and port, but with
                // a new node ID. remove the old entry and
                // replace it with this new ID
                existing.right.removeEntry(existing.left);
                ips.remove(existing.left.getEndpoint().getIP());
                //remove_node(existing, existing_bucket);
            }
        }

        int bucketIndex = findBucket(e.getId());
        RoutingTableBucket bucket = buckets.get(bucketIndex);

        int j = Utils.indexOf(bucket.getLiveNodes(), new FindByKadId(e.getId()));

        if (j != -1) {
            // a new IP address just claimed this node-ID
            // ignore it
            NodeEntry n = bucket.getLiveNodes().get(j);
            if (!n.equals(e.getEndpoint())) return ret;

            // we already have the node in our bucket
            // just move it to the back since it was
            // the last node we had any contact with
            // in this bucket
            assert n.getId().equals(e.getId()) && n.getEndpoint().equals(e.getEndpoint());
            n.setTimeoutCount(0);
            log.debug("table updating node: {}", n);
            return ret;
        }

        if (Utils.indexOf(bucket.getReplacements(), new FindByKadId(e.getId())) != -1) return ret;
/*
        if (m_settings.restrict_routing_ips)
        {
            // don't allow multiple entries from IPs very close to each other
            j = std::find_if(b->begin(), b->end(), boost::bind(&compare_ip_cidr, _1, e));
            if (j != b->end())
            {
                // we already have a node in this bucket with an IP very
                // close to this one. We know that it's not the same, because
                // it claims a different node-ID. Ignore this to avoid attacks
                #ifdef LIBED2K_DHT_VERBOSE_LOGGING
                LIBED2K_LOG(table) << "ignoring node: " << e.id << " " << e.addr
                        << " existing node: "
                        << j->id << " " << j->addr;
                #endif
                return ret;
            }

            j = std::find_if(rb->begin(), rb->end(), boost::bind(&compare_ip_cidr, _1, e));
            if (j != rb->end())
            {
                // same thing bug for the replacement bucket
                #ifdef LIBED2K_DHT_VERBOSE_LOGGING
                LIBED2K_LOG(table) << "ignoring (replacement) node: " << e.id << " " << e.addr
                        << " existing node: "
                        << j->id << " " << j->addr;
                #endif
                return ret;
            }
        }
*/
        // if the node was not present in our list
        // we will only insert it if there is room
        // for it, or if some of our nodes have gone
        // offline
        if (bucket.getLiveNodes().size() < bucketSize) {
            bucket.getLiveNodes().add(e);
            ips.add(e.getEndpoint().getIP());
            return ret;
        }

        // if there is no room, we look for nodes that are not 'pinged',
        // i.e. we haven't confirmed that they respond to messages.
        // Then we look for nodes marked as stale
        // in the k-bucket. If we find one, we can replace it.

        // can we split the bucket?
        boolean canSplit = false;

        if (e.isPinged() && e.failCount() == 0) {
            // only nodes that are pinged and haven't failed
            // can split the bucket, and we can only split
            // the last bucket
            canSplit = (bucketIndex == buckets.size() - 1) && (buckets.size() < KadId.TOTAL_BITS);

            // if the node we're trying to insert is considered pinged,
            // we may replace other nodes that aren't pinged
            //j = std::find_if(b->begin(), b->end(), boost::bind(&node_entry::pinged, _1) == false);

            j = Utils.indexOf(bucket.getLiveNodes(), new Checker<NodeEntry>() {
                @Override
                public boolean check(NodeEntry nodeEntry) {
                    return nodeEntry.isPinged();
                }
            });

            if (j != -1 && !bucket.getLiveNodes().get(j).isPinged()) {
                // j points to a node that has not been pinged.
                // Replace it with this new one
                NodeEntry n = bucket.getLiveNodes().get(j);
                log.debug("table replacing unpinged node {} with {}", n, e);
                ips.remove(n.getEndpoint().getIP());
                bucket.getLiveNodes().remove(j);
                bucket.getLiveNodes().add(e);
                ips.add(e.getEndpoint().getIP());
                return ret;
            }

            // A node is considered stale if it has failed at least one
            // time. Here we choose the node that has failed most times.
            // If we don't find one, place this node in the replacement-
            // cache and replace any nodes that will fail in the future
            // with nodes from that cache.
            NodeEntry staleNode = Collections.max(bucket.getLiveNodes(), new Comparator<NodeEntry>() {
                @Override
                public int compare(NodeEntry o1, NodeEntry o2) {
                    if (o1.failCount() < o2.failCount()) return -1;
                    if (o1.failCount() > o2.failCount()) return 1;
                    return 0;
                }
            });

            if (staleNode != null && staleNode.failCount() > 0) {
                // i points to a node that has been marked
                // as stale. Replace it with this new one
                log.debug("table replacing stale node {} with {}", staleNode, e);
                ips.remove(staleNode.getEndpoint().getIP());
                bucket.getLiveNodes().remove(staleNode);
                bucket.getLiveNodes().add(e);
                ips.add(e.getEndpoint().getIP());
                return ret;
            }
        }

        // if we can't split, try to insert into the replacement bucket

        if (!canSplit) {
            // if we don't have any identified stale nodes in
            // the bucket, and the bucket is full, we have to
            // cache this node and wait until some node fails
            // and then replace it.
            j = Utils.indexOf(bucket.getReplacements(), new FindByKadId(e.getId()));

            // if the node is already in the replacement bucket
            // just return.
            if (j != -1) {
                // if the IP address matches, it's the same node
                // make sure it's marked as pinged
                if (bucket.getReplacements().get(j).getEndpoint().equals(e.getEndpoint())) bucket.getReplacements().get(j).setPinged();
                return ret;
            }

            if (bucket.getReplacements().size() >= bucketSize) {
                // if the replacement bucket is full, remove the oldest entry
                // but prefer nodes that haven't been pinged, since they are
                // less reliable than this one, that has been pinged
                //j = std::find_if(rb->begin(), rb->end(), boost::bind(&node_entry::pinged, _1) == false);

                j = Utils.indexOf(bucket.getReplacements(), new Checker<NodeEntry>() {
                    @Override
                    public boolean check(NodeEntry nodeEntry) {
                        return !nodeEntry.isPinged();
                    }});

                if (j == -1) j = 0;

                ips.remove(bucket.getReplacements().get(j).getEndpoint().getIP());
                bucket.getReplacements().remove(j);
            }

            bucket.getReplacements().add(e);
            ips.add(e.getEndpoint().getIP());
            log.debug("table inserting node in replacement cache: {}", e);
            return ret;
        }

        // this is the last bucket, and it's full already. Split
        // it by adding another bucket
        RoutingTableBucket newBucket = new RoutingTableBucket();
        buckets.add(newBucket);
        // the extra seconds added to the end is to prioritize
        // buckets closer to us when refreshing
        newBucket.setLastActive(Time.currentTime() + Time.seconds(KadId.TOTAL_BITS - buckets.size()));
        //m_buckets.back().last_active = min_time() + seconds(kad_id::kad_total_bits - m_buckets.size());

        //bucket_t& new_bucket = m_buckets.back().live_nodes;
        //bucket_t& new_replacement_bucket = m_buckets.back().replacements;

        // update the iterator and bucket pointers, since we
        // appended a new bucket to the routing table, and all
        // iterators may have been invalidated
        //i = m_buckets.begin() + bucket_index;
        //b = &i->live_nodes;
        //rb = &i->replacements;

        // move any node whose (kad_id::kad_total_bits - distane_exp(m_id, id)) >= (i - m_buckets.begin())
        // to the new bucket
        Iterator<NodeEntry> itr = bucket.getLiveNodes().iterator();

        while(itr.hasNext()) {
            NodeEntry entry = itr.next();
            if (KadId.distanceExp(self, entry.getId()) < KadId.TOTAL_BITS - 1 - bucketIndex) {
                newBucket.getLiveNodes().add(entry);
                itr.remove();
            }
        }

        //for (NodeEntry entry: bucket.getLiveNodes()) {
        //    if (KadId.distanceExp(self, entry.getId()) >= KadId.TOTAL_BITS - 1 - bucketIndex) {
        //        continue;
        //    }

            // this entry belongs in the new bucket
        //    new_bucket.push_back(*j);
        //    j = b->erase(j);
        //}

        // split the replacement bucket as well. If the live bucket
        // is not full anymore, also move the replacement entries
        // into the main bucket
        itr = bucket.getReplacements().iterator();
        while(itr.hasNext()) {
            NodeEntry entry = itr.next();
            if (KadId.distanceExp(self, entry.getId()) >= KadId.TOTAL_BITS - 1 - bucketIndex) {
                if (bucket.getLiveNodes().size() >= bucketSize) {
                    continue;
                }

                bucket.getLiveNodes().add(entry);
            } else {
                if (newBucket.getLiveNodes().size() < bucketSize) {
                    newBucket.getLiveNodes().add(entry);
                } else {
                    newBucket.getReplacements().add(entry);
                }
            }

            itr.remove();
        }

        boolean added = false;

        // now insert the new node in the appropriate bucket
        if (KadId.distanceExp(self, e.getId()) >= KadId.TOTAL_BITS - 1 - bucketIndex) {
            if (bucket.getLiveNodes().size() < bucketSize) {
                bucket.getLiveNodes().add(e);
                added = true;
            }
            else if (bucket.getReplacements().size() < bucketSize) {
                bucket.getReplacements().add(e);
                added = true;
            }
        }
        else
        {
            if (newBucket.getLiveNodes().size() < bucketSize) {
                newBucket.getLiveNodes().add(e);
                added = true;
            }
            else if (newBucket.getReplacements().size() < bucketSize) {
                newBucket.getReplacements().add(e);
                added = true;
            }
        }

        if (added) ips.add(e.getEndpoint().getIP());
        return ret;
    }

    public void nodeFailed(final KadId id, final NetworkIdentifier ep) {
        // if messages to ourself fails, ignore it
        if (id.equals(self)) return;

        RoutingTableBucket bucket = buckets.get(findBucket(id));

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
            log.debug("table NODE FAILED {} uptime {}"
                    , failedNode
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

    void forEach(final NodeEntryFun func) {
        assert func != null;
        for(RoutingTableBucket bucket: buckets) {
            for(final NodeEntry live: bucket.getLiveNodes()) {
                func.fun(live);
            }

            for(final NodeEntry repl: bucket.getReplacements()) {
                func.fun(repl);
            }
        }
    }

    void addRouterNode(final NetworkIdentifier ep) {
        routerNodes.add(ep);
    }

    // was spoofed or not (i.e. pinged == false)
    void heardAbout(final KadId id, final NetworkIdentifier ep) {
        addNode(new NodeEntry(id, ep, false));
    }

    // this function is called every time the node sees
    // a sign of a node being alive. This node will either
    // be inserted in the k-buckets or be moved to the top
    // of its bucket.
    // the return value indicates if the table needs a refresh.
    // if true, the node should refresh the table (i.e. do a find_node
    // on its own id)
    boolean nodeSeen(final KadId id, final NetworkIdentifier ep) {
        return addNode(new NodeEntry(id, ep, true));
    }

    private void copy(int bucketIndex, List<NodeEntry> res, boolean includeFailed, int count) {
        RoutingTableBucket bucket = buckets.get(bucketIndex);
        assert bucket != null;
        for(NodeEntry e: bucket.getLiveNodes()) {
            if (res.size() == count) break;
            if (includeFailed || e.isConfirmed()) {
                res.add(e);
            }
        }
    }

    // fills the vector with the k nodes from our buckets that
    // are nearest to the given id.
    List<NodeEntry> findNode(final KadId target, boolean includeFailed, int count) {
        List<NodeEntry> res = new LinkedList<>();
        if (count == 0) count = bucketSize;

        int i = findBucket(target);
        copy(i, res, includeFailed, count);

        assert res.size() <= count;

        if (res.size() >= count) return res;

        // if we didn't have enough nodes in that bucket
        // we have to reply with nodes from buckets closer
        // to us.
        int j = i;
        ++j;

        for (; j < buckets.size() && res.size() < count; ++j) {
            copy(j, res, includeFailed, count);
            assert res.size() <= count;
            if (res.size() >= count) return res;
        }

        // if we still don't have enough nodes, copy nodes
        // further away from us
        if (i == 0) return res;
        j = i;

        do {
            --j;
            assert j >= 0;
            copy(j, res, includeFailed, count);
            if (res.size() >= count) return res;
        }
        while (j > 0 && res.size() < count);

        return res;
    }
}
