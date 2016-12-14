package org.dkf.jed2k.kad;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.Time;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.kad.traversal.algorithm.*;
import org.dkf.jed2k.kad.traversal.observer.NullObserver;
import org.dkf.jed2k.kad.traversal.observer.Observer;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.Hash;
import org.dkf.jed2k.protocol.kad.*;
import org.dkf.jed2k.util.EndpointSerializer;
import org.dkf.jed2k.util.HashSerializer;
import org.dkf.jed2k.util.KadIdSerializer;

import java.net.InetSocketAddress;
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
        hello.getVersion().assign(PacketCombiner.KADEMLIA_VERSION5_48a);
        hello.getPortTcp().assign(port);
        invoke(hello, ep, new NullObserver(new Single(this, id), ep, id, 0, (byte)0));
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
        Traversal t = new Refresh(this, self);
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

    public void incoming(final Transaction t, final InetSocketAddress address) {
        final Endpoint ep = Endpoint.fromInet(address);
        log.trace("[node] << {}: {}", address, t);

        Observer o = rpc.incoming(t, ep);

        if (o != null) {
            o.reply(t, ep);
            // if we have endpoint's KAD id in packet - use it
            // else use KAD id from observer
            Traversal ta = o.getAlgorithm();
            assert ta != null;
            KadId originId = o.getId();

            if (t instanceof Kad2HelloRes) {
                Kad2HelloRes res = (Kad2HelloRes)t;
                table.nodeSeen(res.getKid()
                        , o.getEndpoint()
                        , res.getPortTcp().intValue()
                        , res.getVersion().byteValue());
            }
            else if (t instanceof Kad2BootstrapRes) {
                // update self in routing table
                table.nodeSeen(((Kad2BootstrapRes)t).getKid()
                    , o.getEndpoint()
                    , ((Kad2BootstrapRes)t).getPortTcp().intValue()
                    , ((Kad2BootstrapRes)t).getVersion().byteValue());

                // register sources
                for(final KadEntry e: ((Kad2BootstrapRes)t).getContacts()) {
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
            if (t instanceof Kad2Ping) {
                Kad2Pong pong = new Kad2Pong();
                pong.getPortUdp().assign(port);
                tracker.write(pong, address);
                log.debug("[node] >> {}: {}", ep, pong);
            }
            else if (t instanceof Kad2HelloReq) {
                Kad2HelloRes hello = new Kad2HelloRes();
                hello.setKid(getSelf());
                hello.getPortTcp().assign(getPort());
                hello.getVersion().assign(PacketCombiner.KADEMLIA_VERSION5_48a);
                tracker.write(hello, address);
                log.debug("[node] >> {}: {}", ep, hello);
            }
            else if (t instanceof Kad2SearchKeysReq) {
                log.debug("[node] temporary ignore kad search key request");
            }
            else if (t instanceof Kad2SearchSourcesReq) {
                log.debug("[node] temporary ignore kad search sources request");
            }
            else if (t instanceof Kad2SearchNotesReq) {
                log.debug("[node] temporary ignore kad search notes request");
            }
            else if (t instanceof Kad2Req) {
                Kad2Req req = (Kad2Req)t;
                if (req.getSearchType() != FindData.KADEMLIA_FIND_NODE
                        && req.getSearchType() != FindData.KADEMLIA_FIND_VALUE
                        && req.getSearchType() != FindData.KADEMLIA_STORE) {
                    log.warn("[node] << {} incorrect search type in packet {}", ep, t);
                }
                else {
                    Kad2Res res = new Kad2Res();
                    List<NodeEntry> entries = table.findNode(req.getTarget(), false, (int)req.getSearchType());
                    res.setTarget(req.getTarget());
                    for(final NodeEntry e: entries) {
                        //res.getResults().add(new KadEntry())
                    }

                    log.debug("[node] temporary do nothing on kad request");
                }
            }
            else {
                log.debug("[node] temporary skip unhandled packet {}", t);
            }
        }
    }

    public boolean invoke(final Transaction t, final Endpoint ep, final Observer o) {
        try {
            if (tracker.write(t, ep.toInetSocketAddress())) {
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

}
