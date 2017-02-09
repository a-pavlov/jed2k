package org.dkf.jed2k;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.Hash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.LinkedList;

/**
 * special kind of session with replaced some methods to help debug application
 * Created by inkpot on 19.08.2016.
 */
public class SessionTrial extends Session {
    private final Logger log = LoggerFactory.getLogger(SessionTrial.class);
    private final HashSet<Endpoint> fileSources = new HashSet<Endpoint>();

    public SessionTrial(Settings st, final LinkedList<Endpoint> peers) {
        super(st);
        fileSources.addAll(peers);
    }

    public void add(final Endpoint endpoint) {
        fileSources.add(endpoint);
    }

    @Override
    void sendSourcesRequest(final Hash h, final long size) {
        Transfer transfer = transfers.get(h);
        if (transfer != null) {
            for(final Endpoint endpoint: fileSources) {
                try {
                    transfer.addPeer(endpoint, 0);
                } catch(JED2KException e) {
                    log.error("add peer to transfer {} error {}", h, e);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "SessionTrial";
    }
}
