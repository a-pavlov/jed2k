package org.dkf.jed2k.kad;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.Time;
import org.dkf.jed2k.Utils;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.kad.traversal.algorithm.*;
import org.dkf.jed2k.kad.traversal.observer.NullObserver;
import org.dkf.jed2k.kad.traversal.observer.Observer;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.Hash;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.Unsigned;
import org.dkf.jed2k.protocol.kad.*;
import org.dkf.jed2k.util.EndpointSerializer;
import org.dkf.jed2k.util.HashSerializer;
import org.dkf.jed2k.util.KadIdSerializer;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by inkpot on 22.11.2016.
 */
@Slf4j
@Getter
public class NodeImpl {

    private final static int SEARCH_BRANCHING = 5;
    private final static int BUCKET_SIZE = 10;
    private final RpcManager rpc;
    private DhtTracker tracker = null;
    private RoutingTable table = null;
    private Set<Traversal> runningRequests = new HashSet<>();
    private final KadId self;
    private int port;
    private IndexedImpl index = new IndexedImpl();
    private int localAddress = 0;
    private boolean firewalled = true;
    private long lastFirewalledCheck = 0;

    public NodeImpl(final DhtTracker tracker, final KadId id, int port) {
        assert tracker != null;
        assert id != null;
        this.tracker = tracker;
        this.rpc = new RpcManager();
        this.self = id;
        this.table = new RoutingTable(id, BUCKET_SIZE);
        this.port = port;
    }

    public void addNode(final Endpoint ep, final KadId id) throws JED2KException {
        Kad2HelloReq hello = new Kad2HelloReq();
        hello.setKid(getSelf());
        hello.getVersion().assign(PacketCombiner.KADEMLIA_VERSION);
        hello.getPortTcp().assign(port);
        invoke(hello, ep, new NullObserver(new Single(this, id), ep, id, 0, (byte)0));
    }

    /**
     * adds new KAD entry to our table with ping(hello request)
     * @param entry - KAD entry item
     * @throws JED2KException
     */
    public void addKadNode(final KadEntry entry) throws JED2KException {
        Kad2HelloReq hello = new Kad2HelloReq();
        hello.setKid(getSelf());
        hello.getVersion().assign(PacketCombiner.KADEMLIA_VERSION);
        hello.getPortTcp().assign(port);
        invoke(hello, entry.getKadEndpoint().getEndpoint()
                , new NullObserver(new Single(this, entry.getKid())
                        , entry.getKadEndpoint().getEndpoint()
                        , entry.getKid()
                        , entry.getKadEndpoint().getPortTcp().intValue()
                        , entry.getVersion()));
    }

    public void addTraversalAlgorithm(final Traversal ta) throws JED2KException {
        if (runningRequests.contains(ta)) throw new JED2KException(ErrorCode.DHT_REQUEST_ALREADY_RUNNING);
        assert !runningRequests.contains(ta);
        runningRequests.add(ta);
    }

    public void removeTraversalAlgorithm(final Traversal ta) {
        assert runningRequests.contains(ta);
        runningRequests.remove(ta);
    }

    public RoutingTable getTable() {
        return table;
    }

    public void tick() {
        rpc.tick();
        if (!runningRequests.isEmpty()) {
            log.trace("[node] running requests {}", runningRequests.size());
        }
        KadId target = table.needRefresh();
        try {
            if (target != null) refresh(target);
        } catch(JED2KException e) {
            log.error("unable to refresh bucket with target {} due to error {}", target, e);
        }

        // start firewalled check when we have at least 5 live nodes and last check was later than 1 hour ago
        if (runningRequests.isEmpty() && table.getSize().getLeft().intValue() > 5 && ((lastFirewalledCheck + Time.hours(1) < Time.currentTime()) || lastFirewalledCheck == 0) ) {
            log.debug("[node] start firewalled check");
            lastFirewalledCheck = Time.currentTime();
            try {
                firewalled();
            } catch(JED2KException e) {
                log.error("[node] unable to start firewalled algorithm {}", e);
            }
        }
    }

    public void searchSources(final KadId id, long size, final Listener l) throws JED2KException {
        log.debug("[node] search sources {}", id);
        Traversal ta = new FindSources(this, id, size, l);
        ta.start();
    }

    public void searchKeywords(final KadId id, final Listener l) throws JED2KException {
        log.debug("[node] search keywords {}", id);
        Traversal ta = new FindKeywords(this, id, l);
        ta.start();
    }

    // not available now
    public void searchNotes(final KadId id) {
        log.debug("[node] search notes {}", id);
    }

    public void refresh(final KadId id) throws JED2KException {
        assert id != null;
        log.debug("[node] refresh on target {}", id);
        Traversal t = new Refresh(this, id);
        t.start();
    }

    public void firewalled() throws JED2KException {
        log.debug("[node] start firewalled check");
        Traversal t = new Firewalled(this, self, null, port);
        t.start();
    }

    public void abort() {
        tracker = null;
    }

    public void bootstrap(final List<Endpoint> nodes) throws JED2KException {
        log.debug("[node] bootstrap with {} nodes", nodes.size());
        Traversal t = new Bootstrap(this, self);
        for(Endpoint ep: nodes) {
            t.addEntry(new KadId(), ep, Observer.FLAG_INITIAL, 0, (byte)0);
        }

        t.start();
    }

    public int getSearchBranching() {
        return SEARCH_BRANCHING;
    }

    public void incoming(final Serializable s, final InetSocketAddress address) {
        final Endpoint ep = Endpoint.fromInet(address);
        log.trace("[node] << {}: {}", address, s);

        Observer o = rpc.incoming(s, ep);

        if (o != null) {
            o.reply(s, ep);
            // if we have endpoint's KAD id in packet - use it
            // else use KAD id from observer
            Traversal ta = o.getAlgorithm();
            assert ta != null;
            KadId originId = o.getId();

            if (s instanceof Kad2HelloRes) {
                Kad2HelloRes res = (Kad2HelloRes)s;
                table.nodeSeen(res.getKid()
                        , o.getEndpoint()
                        , res.getPortTcp().intValue()
                        , res.getVersion().byteValue());
            }
            else if (s instanceof Kad2BootstrapRes) {
                // update self in routing table
                table.nodeSeen(((Kad2BootstrapRes)s).getKid()
                    , o.getEndpoint()
                    , ((Kad2BootstrapRes)s).getPortTcp().intValue()
                    , ((Kad2BootstrapRes)s).getVersion().byteValue());

                // register sources
                for(final KadEntry e: ((Kad2BootstrapRes)s).getContacts()) {
                    table.nodeSeen(e.getKid()
                            , e.getKadEndpoint().getEndpoint()
                            , e.getKadEndpoint().getPortTcp().intValue()
                            , e.getVersion());
                }
            } else {
                // update routing table with information from observer due to can't find this information in incoming packet
                table.nodeSeen(o.getId(), o.getEndpoint(), o.getPortTcp(), o.getVersion());
            }
        } else {
            // process incoming requests here
            if (s instanceof Kad2Ping) {
                Kad2Pong pong = new Kad2Pong();
                pong.getPortUdp().assign(port);
                tracker.write(pong, address);
                log.debug("[node] >> {}: {}", ep, pong);
            }
            else if (s instanceof Kad2HelloReq) {
                Kad2HelloRes hello = new Kad2HelloRes();
                hello.setKid(getSelf());
                hello.getPortTcp().assign(getPort());
                hello.getVersion().assign(PacketCombiner.KADEMLIA_VERSION);
                tracker.write(hello, address);
                log.debug("[node] >> {}: {}", ep, hello);
            }
            else if (s instanceof Kad2SearchKeysReq) {
                log.debug("[node] temporary ignore kad search key request");
            }
            else if (s instanceof Kad2SearchSourcesReq) {
                log.debug("[node] temporary ignore kad search sources request");
            }
            else if (s instanceof Kad2SearchNotesReq) {
                log.debug("[node] temporary ignore kad search notes request");
            }
            else if (s instanceof Kad2Req) {
                Kad2Req req = (Kad2Req)s;
                int searchType = req.getSearchType() & 0x1F;
                if (searchType != FindData.KADEMLIA_FIND_NODE
                        && searchType != FindData.KADEMLIA_FIND_VALUE
                        && searchType != FindData.KADEMLIA_STORE) {
                    log.warn("[node] << {} incorrect search type in packet {} calculated search type is {}", ep, s, searchType);
                }
                else {
                    Kad2Res res = new Kad2Res();
                    List<NodeEntry> entries = table.findNode(req.getTarget(), false, searchType);
                    res.setTarget(req.getTarget());
                    for(final NodeEntry e: entries) {
                        res.getResults().add(new KadEntry(e.getId()
                                , new KadEndpoint(e.getEndpoint().getIP(), e.getEndpoint().getPort(), e.getPortTcp())
                                , e.getVersion()));
                    }

                    tracker.write(res, address);
                    log.debug("[node] >> {}: {}", ep, res);
                }
            }
            else if (s instanceof Kad2BootstrapReq) {
                List<NodeEntry> entries = table.forEach(new Filter<NodeEntry>() {
                    private int counter = 20;
                    @Override
                    public boolean allow(NodeEntry nodeEntry) {
                        --counter;
                        if (counter >= 0) {
                            return true;
                        }
                        return false;
                    }
                }, new Filter<NodeEntry>() {
                    @Override
                    public boolean allow(NodeEntry nodeEntry) {
                        return false;
                    }
                });

                if (!entries.isEmpty()) {
                    Kad2BootstrapRes kbr = new Kad2BootstrapRes();
                    kbr.setKid(getSelf());
                    kbr.setVersion(Unsigned.uint8(PacketCombiner.KADEMLIA_VERSION));
                    kbr.setPortTcp(Unsigned.uint16(getPort()));

                    for (NodeEntry ne : entries) {
                        kbr.getContacts().add(new KadEntry(ne.getId()
                                , new KadEndpoint(ne.getEndpoint().getIP(), ne.getEndpoint().getPort(), ne.getPortTcp())
                                , ne.getVersion()));
                    }

                    tracker.write(kbr, address);
                } else {
                    log.debug("[node] entries list is empty, send nothing for bootstrap res");
                }
            }
            else if (s instanceof Kad2PublishKeysReq) {
                Kad2PublishKeysReq pubKeys = (Kad2PublishKeysReq)s;
                log.debug("[node] publish keys {}", pubKeys.getSources().size());
                for(KadSearchEntry kse: pubKeys.getSources()) {
                    if (index != null) {
                        index.addKeyword(pubKeys.getKeywordId(), kse, Time.currentTime());
                    } else {
                        log.debug("[node] not added {} size {}", kse);
                    }
                }
            }
            else if (s instanceof Kad2PublishSourcesReq) {
                Kad2PublishSourcesReq pubSrc = (Kad2PublishSourcesReq)s;
                log.debug("[node] publish sources {}", pubSrc.getFileId());
                if (index != null) {
                    index.addSource(pubSrc.getFileId(), pubSrc.getSource(), Time.currentTime());
                } else {
                    log.trace("[node] not indexed source ip {} port {} portTcp {} size {}", pubSrc.getSource());
                }
            }
            else if (s instanceof Kad2FirewalledReq) {
                log.debug("[node] firewalled request received {}", address);
                Kad2FirewalledRes kfr = new Kad2FirewalledRes();
                kfr.setIp(ep.getIP());
                tracker.write(kfr, address);
            }
            else if (s instanceof Kad2SearchKeysReq) {
                if (index != null) {
                    sendSearchResult(address, ((Kad2SearchKeysReq) s).getTarget(), index.getFileByHash(((Kad2SearchKeysReq) s).getTarget()));
                } else {
                    log.debug("[node] index is not created, unable to answer for search keywords {}", ((Kad2SearchKeysReq) s).getTarget());
                }
            }
            else if (s instanceof Kad2SearchSourcesReq) {
                if (index != null) {
                    sendSearchResult(address, ((Kad2SearchSourcesReq) s).getTarget(), index.getSourceByHash(((Kad2SearchSourcesReq) s).getTarget()));
                } else {
                    log.debug("[node] index is not created, unable to answer for search sources {}", ((Kad2SearchSourcesReq) s).getTarget());
                }
            }
            else {
                log.debug("[node] temporary skip unhandled packet {}", s);
            }
        }
    }

    private void sendSearchResult(final InetSocketAddress address, final KadId target, final Collection<IndexedImpl.Published> publishes) {
        if (publishes != null) {
            int collected = 0;
            Kad2SearchRes keywordsSearchRes = new Kad2SearchRes();

            for(final IndexedImpl.Published p: publishes) {
                ++collected;
                keywordsSearchRes.getResults().add(p.getEntry());
                if (collected == 50) {
                    collected = 0;
                    tracker.write(keywordsSearchRes, address);
                    keywordsSearchRes = new Kad2SearchRes();
                }
            }

            assert collected >= 0;

            if (collected != 0) {
                tracker.write(keywordsSearchRes, address);
            }
        } else {
            log.debug("[node] no data for search request {}", target);
        }
    }

    public boolean invoke(final Serializable s, final Endpoint ep, final Observer o) {
        try {
            if (tracker.write(s, ep.toInetSocketAddress())) {
                // register transaction if packet was sent
                rpc.invoke(o);
                o.setWasSent(true);
                o.setFlags(o.getFlags() | Observer.FLAG_QUERIED);
                o.setSentTime(Time.currentTime());
                log.debug("[node] invoked {}", o);
                return true;
            } else {
                log.debug("[node] invoke failed without error {}", o);
            }
        } catch(final JED2KException e) {
            log.error("[node] invoke failed {} with error {}", o, e);
        }

        return false;
    }

    public void logStatus() {
        for(Traversal ta: runningRequests) {
            log.info(ta.toString());
        }
    }

    public String getRoutingTableStatus() {
        Gson gson = new GsonBuilder().setPrettyPrinting()
                .registerTypeAdapter(KadId.class, new KadIdSerializer())
                .registerTypeAdapter(Hash.class, new HashSerializer())
                .registerTypeAdapter(Endpoint.class, new EndpointSerializer())
                .create();

        return gson.toJson(table);
    }

    void setAddress(int localAddress) {
        this.localAddress = localAddress;
    }

    public void processAddresses(int addresses[]) {
        int matches = 0;
        for(final int ip: addresses) {
            log.debug("[node] local/external {}/{}", Utils.ip2String(localAddress), Utils.ip2String(ip));
            if ((ip != 0) && (ip == localAddress)) {
                matches++;
            }
        }

        firewalled = (matches != addresses.length);
        log.debug("[node] firewalled {} matches {}", firewalled?"TRUE":"FALSE", matches);
    }

    public boolean isFirewalled() {
        return firewalled;
    }
}
