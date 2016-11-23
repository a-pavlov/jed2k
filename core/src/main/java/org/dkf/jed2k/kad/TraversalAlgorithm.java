package org.dkf.jed2k.kad;

import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.protocol.Hash;
import org.dkf.jed2k.protocol.kad.KadId;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by inkpot on 21.11.2016.
 */
@Slf4j
public class TraversalAlgorithm {
    private NodeImpl nodeImpl;
    private KadId target;
    List<Observer> results = new ArrayList<>();

    int invokeCount = 0;
    int branchFactor = 0;
    int responses = 0;
    int timeouts = 0;
    int numTargetNodes = 0;

    private static final byte preventRequest = 1;
    private static final byte shortTimeout = 2;

    protected Observer newObserver(final InetSocketAddress endpoint, final KadId id) {
        return null;
    }

    protected boolean invoke(final Observer o) {
        return false;
    }

    protected void done() {
        results.clear();
    }

    protected void init() {
        // update the last activity of this bucket
        //m_node.m_table.touch_bucket(m_target);
        branchFactor = 5; // TODO - use value from DHT settings
        //m_node.add_traversal_algorithm(this);
    }

    protected void addRequests() {
        int resultsTarget = numTargetNodes;

        // Find the first node that hasn't already been queried.
        for (int i = 0; i != results.size() && resultsTarget > 0 && invokeCount < branchFactor; ++i) {
            Observer o = results.get(i);
            if ((o.getFlag() & Observer.FLAG_ALIVE) != 0) --resultsTarget;
            if ((o.getFlag() & Observer.FLAG_QUERIED) != 0) continue;
            log.debug("traversal: {} nodes-left: {} invoke-count: {} branch-factor: {}",
                    name(), results.size(), invokeCount, branchFactor);

            o.setFlag((byte)(o.getFlag() | Observer.FLAG_QUERIED));

            if (invoke(o)) {
                assert invokeCount >= 0;
                ++invokeCount;
            } else {
                o.setFlag((byte)(o.getFlag() | Observer.FLAG_FAILED));
            }
        }
    }

    protected void addRouterEntries() {
        // TODO - add content here after node implementation completed
    }

    public String name() {
        return "traversal algorithm";
    }

    public TraversalAlgorithm(final NodeImpl ni, final KadId t) {
        nodeImpl = ni;
        target = t;
    }

    public void addEntry(final KadId id, final InetSocketAddress addr, byte flags) {
        //TODO check this assert later
        //LIBED2K_ASSERT(m_node.m_rpc.allocation_size() >= sizeof(find_data_observer));
        /*
        this is part of limit algorithm of requests - will be implemented later
        void* ptr = m_node.m_rpc.allocate_observer();
        if (ptr == 0)
        {
            #ifdef LIBED2K_DHT_VERBOSE_LOGGING
            LIBED2K_LOG(traversal) << "[" << this << ":" << name()
                    << "] failed to allocate memory for observer. aborting!";
            #endif
            done();
            return;
        }
        */
        Observer o = newObserver(addr, id);

        if (id.isAllZeros()) {
            o.setId(new KadId(Hash.random(false)));
            o.setFlag((byte)(o.getFlag() | Observer.FLAG_NO_ID));
        }

        o.setFlag((byte)(o.getFlag() | flags));

        int pos = Collections.binarySearch(results, o, new Comparator<Observer>() {
            @Override
            public int compare(Observer o1, Observer o2) {
                return KadId.compareRef(o1.getId(), o2.getId(), target);
            }
        });

        if (pos < 0) {

            /*
            maybe add this later
            if (m_node.settings().restrict_search_ips
                    && !(flags & observer::flag_initial))
            {
                // don't allow multiple entries from IPs very close to each other
                std::vector<observer_ptr>::iterator j = std::find_if(
                    m_results.begin(), m_results.end(), boost::bind(&compare_ip_cidr, _1, o));

                if (j != m_results.end())
                {
                    // we already have a node in this search with an IP very
                    // close to this one. We know that it's not the same, because
                    // it claims a different node-ID. Ignore this to avoid attacks
                    #ifdef LIBED2K_DHT_VERBOSE_LOGGING
                    LIBED2K_LOG(traversal) << "ignoring DHT search entry: " << o->id()
                            << " " << o->target_addr()
                            << " existing node: "
                            << (*j)->id() << " " << (*j)->target_addr();
                    #endif
                    return;
                }
            }
            */

            log.debug("traversal {} adding result: {} {} distance ", name(), id, addr, KadId.distanceExp(target, o.getId()));
            results.add(((pos + 1)*-1), o);
        }

        if (results.size() > 100) {
            for (int i = 100; i < results.size(); ++i)
                results.get(i).setWasAbandoned(true);
            while(results.size() > 100) results.remove(results.size() - 1);
        }
    }

    public void failed(final Observer o, int flags) {
        assert invokeCount >= 0;

        if (results.isEmpty()) return;

        assert (o.getFlag() & Observer.FLAG_QUERIED) != 0;

        if ((flags & shortTimeout) != 0) {
            // short timeout means that it has been more than
            // two seconds since we sent the request, and that
            // we'll most likely not get a response. But, in case
            // we do get a late response, keep the handler
            // around for some more, but open up the slot
            // by increasing the branch factor
            if ((o.getFlag() & Observer.FLAG_SHORT_TIMEOUT) == 0)
                ++branchFactor;
            o.setFlag((byte)(o.getFlag() | Observer.FLAG_SHORT_TIMEOUT));
            log.debug("traversal {} first chance timeout {} branch-factor: {} invoke-count: {}", name(), o.getId(), branchFactor, invokeCount);
        }
        else {
            o.setFlag((byte)(o.getFlag() | Observer.FLAG_FAILED));
            // if this flag is set, it means we increased the
            // branch factor for it, and we should restore it
            if ((o.getFlag() & Observer.FLAG_SHORT_TIMEOUT) != 0) --branchFactor;
            log.debug("traversal {} failed {} branch-factor: {} invoke-count: {}", name(), branchFactor, invokeCount);

            // don't tell the routing table about
            // node ids that we just generated ourself
            if ((o.getFlag() & Observer.FLAG_NO_ID) == 0) {
                // TODO - tell table we failed
                //m_node.m_table.node_failed(o->id(), o->target_ep());
            }

            ++timeouts;
            --invokeCount;
            assert invokeCount >= 0;
        }

        if ((flags & preventRequest) != 0) {
            --branchFactor;
            if (branchFactor <= 0) branchFactor = 1;
        }

        addRequests();
        if (invokeCount == 0) done();
    }

    public void start() {
        if (results.isEmpty()) addRouterEntries();
        init();
        addRequests();
        if (invokeCount == 0) done();
    }


    public void finished(final Observer o) {
        boolean contains = results.contains(o);
        // we have this observer or it was abandoned(size > 100)
        assert contains || results.size() == 100;


        // if this flag is set, it means we increased the
        // branch factor for it, and we should restore it
        if ((o.getFlag() & Observer.FLAG_SHORT_TIMEOUT) != 0) --branchFactor;

        assert (o.getFlag() & Observer.FLAG_QUERIED) != 0;
        o.setFlag((byte)(o.getFlag() | Observer.FLAG_ALIVE));

        ++responses;
        --invokeCount;
        assert invokeCount >= 0;
        addRequests();
        if (invokeCount == 0) done();
    }
}
