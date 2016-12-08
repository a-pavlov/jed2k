package org.dkf.jed2k.kad.traversal.algorithm;

import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.Utils;
import org.dkf.jed2k.kad.NodeImpl;
import org.dkf.jed2k.kad.traversal.observer.Observer;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.Hash;
import org.dkf.jed2k.protocol.kad.KadId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by inkpot on 21.11.2016.
 */
@Slf4j
public abstract class Traversal {
    protected NodeImpl nodeImpl;
    protected KadId target;
    List<Observer> results = new ArrayList<>();

    int invokeCount = 0;
    int branchFactor = 0;
    int responses = 0;
    int timeouts = 0;
    int numTargetNodes = 0;

    public static final int PREVENT_REQUEST = 1;
    public static final int SHORT_TIMEOUT = 2;
    public static final int MAX_RESULT_COUNT = 100;

    public Traversal(final NodeImpl ni, final KadId t) {
        assert t != null;
        assert !t.isAllZeros();
        assert ni != null;
        nodeImpl = ni;
        target = t;
        numTargetNodes = nodeImpl.getTable().getBucketSize()*2;
    }

    public abstract Observer newObserver(final Endpoint endpoint, final KadId id);

    public abstract boolean invoke(final Observer o);

    public boolean containsNewNodes() {
        return true;
    }

    public void done() {
        log.debug("[traversal] done, results size {}", results.size());
        log.debug(toString());
        results.clear();
        nodeImpl.removeTraversalAlgorithm(this);
    }

    public void init() {
        // update the last activity of this bucket
        nodeImpl.getTable().touchBucket(target);
        branchFactor = nodeImpl.getSearchBranching();
        nodeImpl.addTraversalAlgorithm(this);
    }

    /**
     * run new requests using current results collection
     * execute new requests until we have less than resultsTarget alive nodes or no new candidates
     */
    protected void addRequests() {
        int resultsTarget = numTargetNodes;
        // FindData the first node that hasn't already been queried.
        for (int i = 0; i != results.size() && resultsTarget > 0 && invokeCount < branchFactor; ++i) {
            Observer o = results.get(i);
            if (Utils.isBit(o.getFlags(), Observer.FLAG_ALIVE)) --resultsTarget;
            if (Utils.isBit(o.getFlags(), Observer.FLAG_QUERIED)) continue;
            log.debug("[traversal] {} nodes-left: {} invoke-count: {} branch-factor: {}",
                    getName(), results.size(), invokeCount, branchFactor);

            o.setFlags(o.getFlags() | Observer.FLAG_QUERIED);

            if (invoke(o)) {
                assert invokeCount >= 0;
                log.debug("[traversal] add request {}", o);
                ++invokeCount;
            } else {
                o.setFlags(o.getFlags() | Observer.FLAG_FAILED);
            }
        }
    }

    protected void addRouterEntries() {
        log.debug("[traversal] using router nodes to initiate traversal algorithm. count {}", nodeImpl.getTable().getRouterNodes().size());
        for(final Endpoint ep: nodeImpl.getTable().getRouterNodes()) {
            addEntry(new KadId(), ep, Observer.FLAG_INITIAL);
        }
    }

    public String getName() {
        return "[ta]";
    }

    public void addEntry(final KadId id, final Endpoint addr, byte flags) {
        log.debug("[traversal] add entry {} {}", id, addr);
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
            o.setFlags(o.getFlags() | Observer.FLAG_NO_ID);
        }

        o.setFlags(o.getFlags() | flags);

        int pos = Collections.binarySearch(results, o, new Comparator<Observer>() {
            @Override
            public int compare(Observer o1, Observer o2) {
                return KadId.compareRef(o1.getId(), o2.getId(), target);
            }
        });

        log.trace("[traversal] position for entry {}", pos);
        assert pos < results.size();

        if (pos < 0 || !results.get(pos).equals(id)) {

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

            log.trace("[traversal] {} adding result: {} {} distance {}", getName(), id, addr, KadId.distanceExp(target, o.getId()));
            int insertPos = (pos < 0)?(pos + 1)*-1:pos;
            assert insertPos >= 0;
            assert insertPos <= results.size();
            results.add(insertPos, o);
        }

        if (results.size() > MAX_RESULT_COUNT) {
            for (int i = MAX_RESULT_COUNT; i < results.size(); ++i)
                results.get(i).setWasAbandoned(true);
            while(results.size() > MAX_RESULT_COUNT) results.remove(results.size() - 1);
        }
    }

    public void failed(final Observer o, int flags) {
        log.debug("[traversal] failed {} flags {}", o, flags);
        assert invokeCount >= 0;

        if (results.isEmpty()) return;

        assert (o.getFlags() & Observer.FLAG_QUERIED) != 0;

        if (Utils.isBit(flags, SHORT_TIMEOUT)) {
            // short timeout means that it has been more than
            // two seconds since we sent the request, and that
            // we'll most likely not get a response. But, in case
            // we do get a late response, keep the handler
            // around for some more, but open up the slot
            // by increasing the branch factor
            assert Utils.isBit(o.getFlags(), Observer.FLAG_SHORT_TIMEOUT) != true;
            //if (!Utils.isBit(o.getFlags(), Observer.FLAG_SHORT_TIMEOUT))
            ++branchFactor;
            o.setFlags(o.getFlags() | Observer.FLAG_SHORT_TIMEOUT);
            log.debug("[traversal] {} first chance timeout {} branch-factor: {} invoke-count: {}", getName(), o.getId(), branchFactor, invokeCount);
        }
        else {
            o.setFlags(o.getFlags() | Observer.FLAG_FAILED);
            // if this flag is set, it means we increased the
            // branch factor for it, and we should restore it
            if (Utils.isBit(o.getFlags(), Observer.FLAG_SHORT_TIMEOUT)) --branchFactor;

            // don't tell the routing table about
            // node ids that we just generated ourself
            if ((o.getFlags() & Observer.FLAG_NO_ID) == 0) {
                nodeImpl.getTable().nodeFailed(o.getId(), o.getEndpoint());
            }

            ++timeouts;
            --invokeCount;
            assert invokeCount >= 0;
            log.debug("[traversal] {} {} failed branch-factor: {} invoke-count: {}", o, getName(), branchFactor, invokeCount);
        }

        if (Utils.isBit(flags, PREVENT_REQUEST)) {
            --branchFactor;
            if (branchFactor <= 0) branchFactor = 1;
            log.debug("[traversal] prevent request branch-factor {}", branchFactor);
        }

        log.debug("[traversal] failed end invoke-count {} branch-factor {}", invokeCount, branchFactor);

        addRequests();
        if (invokeCount == 0) done();
    }

    public void start() {
        log.debug("[traversal] start");
        if (results.isEmpty()) addRouterEntries();
        init();
        addRequests();
        if (invokeCount == 0) done();
    }

    public void finished(final Observer o) {
        log.debug("[traversal] finished {} invoke-count {}", o, invokeCount);
        boolean contains = results.contains(o);
        // we have this observer or it was abandoned(size > MAX_RESULTS_COUNT)
        assert contains || results.size() == MAX_RESULT_COUNT;

        // if this flag is set, it means we increased the
        // branch factor for it, and we should restore it
        if (Utils.isBit(o.getFlags(), Observer.FLAG_SHORT_TIMEOUT)) --branchFactor;

        assert (o.getFlags() & Observer.FLAG_QUERIED) != 0;
        o.setFlags(o.getFlags() | Observer.FLAG_ALIVE);

        ++responses;
        --invokeCount;
        log.debug("[traversal] finished invoke count {}", invokeCount);
        assert invokeCount >= 0;
        addRequests();
        if (invokeCount == 0) done();
    }

    /**
     *  traverse new node - add to results and prepare for requesting
     * @param id of new node
     * @param ep address of new node
     */
    public void traverse(final Endpoint ep, final KadId id) {
        nodeImpl.getTable().heardAbout(id, ep);
        addEntry(id, ep, (byte)0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Traversal that = (Traversal) o;

        return target.equals(that.target) && getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        return target.hashCode() + getName().hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Traversal: ")
                .append(target)
                .append(" invoke-count ")
                .append(invokeCount)
                .append(" branch-factor ")
                .append(branchFactor)
                .append(" responses ")
                .append(responses)
                .append(" timeouts ")
                .append(timeouts)
                .append(" num targets ")
                .append(numTargetNodes);
        if (!results.isEmpty()) sb.append("\nresults:\n");
        for(final Observer o: results) {
            sb.append(o)
                    .append(o.getFlagsStr())
                    .append("\n");
        }

        return sb.toString();
    }
}
