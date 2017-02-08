package org.dkf.jed2k.kad.traversal.algorithm;

import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.Utils;
import org.dkf.jed2k.kad.Filter;
import org.dkf.jed2k.kad.Listener;
import org.dkf.jed2k.kad.NodeEntry;
import org.dkf.jed2k.kad.NodeImpl;
import org.dkf.jed2k.kad.traversal.observer.FirewalledObserver;
import org.dkf.jed2k.kad.traversal.observer.Observer;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.kad.Kad2FirewalledReq;
import org.dkf.jed2k.protocol.kad.KadId;

import java.util.List;

/**
 * Created by apavlov on 23.01.17.
 */
@Slf4j
public class Firewalled extends Direct {
    public static final int MAX_REASONABLE_RESPONSES = 2;
    private int finishedCount = 0;
    private int portTcp;
    private int externalIps[] = {0, 0};


    public Firewalled(NodeImpl ni, KadId t, Listener listener, int portTcp) {
        super(ni, t, listener);
        this.portTcp = portTcp;

        List<NodeEntry> nodes = ni.getTable().forEach(new Filter<NodeEntry>() {
            @Override
            public boolean allow(NodeEntry nodeEntry) {
                return true;
            }
        }, null);

        for(int i = 0; i < Math.min(50, nodes.size()); ++i) {
            final NodeEntry e = nodes.get(i);
            results.add(newObserver(e.getEndpoint(), e.getId(), e.getPortTcp(), e.getVersion()));
        }

        log.debug("[firewalled] initial size {}", results.size());
    }

    @Override
    public Observer newObserver(Endpoint endpoint, KadId id, int portTcp, byte version) {
        return new FirewalledObserver(this, endpoint, id, portTcp, version);
    }

    @Override
    public boolean invoke(Observer o) {
        // actual send only when we have less than MAX_REASONABLE_RESPONSES responses
        if (finishedCount < MAX_REASONABLE_RESPONSES) {
            Kad2FirewalledReq fr = new Kad2FirewalledReq();
            fr.setId(target);
            fr.setOptions((byte) 0);
            fr.getPortTcp().assign(portTcp);
            return nodeImpl.invoke(fr, o.getEndpoint(), o);
        }

        return false;
    }

    @Override
    public void finished(Observer o) {
        super.finished(o);

        // store first MAX_REASONABLE_RESPONSES responses IPs and stop next requests
        if (responses <= MAX_REASONABLE_RESPONSES) {
            assert responses > 0;
            int ip = ((FirewalledObserver)o).getIp();
            if (ip != 0) {
                externalIps[responses - 1] = ip;
            }
        }

        if (responses == MAX_REASONABLE_RESPONSES) {
            log.debug("[firewalled] has {} responses", MAX_REASONABLE_RESPONSES);
            for(int i: externalIps) {
                log.debug("[firewalled] ip: {}", Utils.ip2String(i));
            }
        }
    }

    @Override
    public void done() {
        super.done();
        nodeImpl.processAddresses(externalIps);
    }
}
