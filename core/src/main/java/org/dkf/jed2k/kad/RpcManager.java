package org.dkf.jed2k.kad;

import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.Time;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.kad.KadId;
import org.dkf.jed2k.protocol.kad.Transaction;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by inkpot on 21.11.2016.
 */
@Slf4j
public class RpcManager {
    private static int SHORT_TIMEOUT = 2;
    private static int TIMEOUT = 12;

    private boolean destructing = false;
    private List<Observer> transactions = new LinkedList<>();

    public void invoke(final Transaction t, final Endpoint ep, final Observer o) {
        transactions.add(o);
    }

    public Observer incoming(final Transaction t, final Endpoint ep, final KadId id) {
        if (destructing) return null;

        Iterator<Observer> itr = transactions.iterator();
        Observer o = null;
        while(itr.hasNext()) {
            o = itr.next();
            assert o != null;
            if (o.getTransactionId() == t.getTransactionId() && o.getTarget().equals(ep)
                    && (t.getRequesterId().isAllZeros() || o.getTarget().equals(t.getRequesterId()))) {
                itr.remove();
                break;
            }

            o = null;
        }

        if (o == null) {
            log.debug("reply with unknown transaction id: {} from {}", t, ep);
        } else {
            log.debug("reply {} from {}", t, ep);
            o.reply(t, ep);
        }

        return o;
/*
        *id = extract_packet_node_id(t);

        // we have no node_id in packet - set it from observer
        if (*id == node_id::invalid) *id = o->id();

        // we found an observer for this reply, hence the node is not spoofing
        // add it to the routing table
        return m_table.node_seen(*id, target);
        */
    }

    public void unreachable(final Endpoint ep) {
        log.debug("port unreachable {}", ep);

        Iterator<Observer> itr = transactions.iterator();
        while(itr.hasNext()) {
            Observer o = itr.next();
            if (o.getEndpoint().equals(ep)) {
                log.debug("found unreachable transaction {}", ep);
                itr.remove();
                o.timeout();
                break;
            }
        }
    }

    public void abort() {
        assert !destructing;
        destructing = true;
        for(final Observer o : transactions) {
            o.abort();
        }

        transactions.clear();
    }

    public void tick() {
        //	look for observers that have timed out
        if (transactions.isEmpty()) return;

        List<Observer> timeouts = new LinkedList<>();

        long now = Time.currentTime();
        final Long[] last = {new Long(0)};

        transactions.forEach(new Consumer<Observer>() {
            @Override
            public void accept(Observer observer) {
                assert observer.getSentTime() >= last[0];
                last[0] = observer.getSentTime();
            }
        });

        Iterator<Observer> itr = transactions.iterator();
        while(itr.hasNext()) {
            Observer o = itr.next();
            // if we reach an observer that hasn't timed out
            // break, because every observer after this one will
            // also not have timed out yet
            long diff = now - o.getSentTime();
            if (diff < Time.seconds(TIMEOUT)) {
                break;
            }

            log.debug("rpc timeout {}", o);
            itr.remove();
            timeouts.add(o);
        }

        for(final Observer o: timeouts) {
            o.timeout();
        }

        timeouts.clear();

        itr = transactions.iterator();
        while(itr.hasNext()) {
            Observer o = itr.next();
            // if we reach an observer that hasn't timed out
            // break, because every observer after this one will
            // also not have timed out yet
            long diff = now - o.getSentTime();
            if (diff < Time.seconds(SHORT_TIMEOUT)) {
                break;
            }

            if (o.hasShortTimeout()) continue;
            timeouts.add(o);
        }

        for (Observer timeout : timeouts) {
            timeout.shortTimeout();
        }
    }
}
