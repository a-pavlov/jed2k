package org.dkf.jed2k.kad;

import org.dkf.jed2k.protocol.kad.*;

import java.net.InetSocketAddress;

/**
 * Created by inkpot on 03.03.2017.
 * interface for processing KAD requests
 */
public interface ReqDispatcher {

    /**
     * dispatch packet to processing and answering. if no address provided - address == null
     * no answer should be produced
     * @param p dispatchable and serializable packet
     * @param address address of origin, can be null
     */
    void process(final Kad2Ping p, final InetSocketAddress address);
    void process(final Kad2HelloReq p, final InetSocketAddress address);
    void process(final Kad2SearchNotesReq p, final InetSocketAddress address);
    void process(final Kad2Req p, final InetSocketAddress address);
    void process(final Kad2BootstrapReq p, final InetSocketAddress address);
    void process(final Kad2PublishKeysReq p, final InetSocketAddress address);
    void process(final Kad2PublishSourcesReq p, final InetSocketAddress address);
    void process(final Kad2FirewalledReq p, final InetSocketAddress address);
    void process(final Kad2SearchKeysReq p, final InetSocketAddress address);
    void process(final Kad2SearchSourcesReq p, final InetSocketAddress address);
}
