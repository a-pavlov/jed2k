package org.dkf.jed2k.kad;

import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.Time;
import org.dkf.jed2k.Utils;
import org.dkf.jed2k.kad.traversal.observer.Observer;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.kad.Transaction;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by inkpot on 21.11.2016.
 */
@Slf4j
public class RpcManager {
    private static int SHORT_TIMEOUT = 2;
    private static int TIMEOUT = 12;

    private boolean destructing = false;
    private List<Observer> transactions = new LinkedList<>();

    /**
     *  register observer in RPC manager only for future processing responses and timeouts
     *  @param o observer
     */
    public void invoke(final Observer o) {
        log.debug("[rpc] invoke {}", o);
        transactions.add(o);
    }

    public Observer incoming(final Transaction t, final Endpoint ep) {
        if (destructing) return null;
        log.trace("[rpc] incoming {} {}", ep, t);

        Iterator<Observer> itr = transactions.iterator();
        Observer o = null;
        while(itr.hasNext()) {
            o = itr.next();
            assert o != null;
            assert Utils.isBit(o.getFlags(), Observer.FLAG_QUERIED);

            log.trace("[rpc] check observer {}", o);

            /**
             * search for source observer using artificial transaction id, endpoint
             * and if available(packet contains target KAD id) target KAD id in observer and target KAD id in incoming packet
             */
            if (o.getTransactionId() == t.getTransactionId() && o.getTarget().getIP() == ep.getIP()
                    /*&& (t.getTargetId().isAllZeros() || o.getTarget().equals(t.getTargetId()))*/) {
                log.debug("[rpc] reply {} from {}", t, ep);
                itr.remove();
                break;
            }

            o = null;
        }

        if (o == null) {
            log.debug("[rpc] reply with unknown transaction id: {} from {}", t, ep);
        }

        return o;
    }

    public void unreachable(final Endpoint ep) {
        log.debug("[rpc] port unreachable {}", ep);

        Iterator<Observer> itr = transactions.iterator();
        while(itr.hasNext()) {
            Observer o = itr.next();
            if (o.getEndpoint().equals(ep)) {
                log.debug("[rpc] found unreachable transaction {}", ep);
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

        for(final Observer o: transactions) {
            assert o.getSentTime() >= last[0];
            last[0] = o.getSentTime();
        }

        Iterator<Observer> itr = transactions.iterator();
        while(itr.hasNext()) {
            Observer o = itr.next();
            // if we reach an observer that hasn't timed out
            // break, because every observer after this one will
            // also not have timed out yet
            long diff = now - o.getSentTime();
            if (diff < Time.seconds(TIMEOUT)) {
                log.debug("[rpc] no timeout {} < {} time {}, send time {}", diff, Time.seconds(TIMEOUT), now, o.getSentTime());
                break;
            }

            log.debug("[rpc] timeout {}", o);
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

        for (Observer o : timeouts) {
            o.shortTimeout();
        }

    }
}
