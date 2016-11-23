package org.dkf.jed2k.kad;

import lombok.Data;
import org.dkf.jed2k.Time;
import org.dkf.jed2k.protocol.kad.KadId;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by inkpot on 23.11.2016.
 */
public class RoutingTable {

    @Data
    private static class RoutingTableNode {
        private ArrayList<NodeEntry> replacements = new ArrayList<>();
        private ArrayList<NodeEntry> live_nodes = new ArrayList<>();
        private long lastActive = Time.currentTime();
    }

    private ArrayList<RoutingTableNode> buckets = new ArrayList<>();
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
    Set<InetSocketAddress> routerNodes = new HashSet<>();

    // these are all the IPs that are in the routing
    // table. It's used to only allow a single entry
    // per IP in the whole table. Currently only for
    // IPv4
    //MultiSemultiset<address_v4::bytes_type> m_ips;

    private int bucketSize;

}
