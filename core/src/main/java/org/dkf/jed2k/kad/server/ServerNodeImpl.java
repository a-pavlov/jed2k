package org.dkf.jed2k.kad.server;

import org.dkf.jed2k.kad.ReqDispatcher;
import org.dkf.jed2k.protocol.kad.*;
import org.postgresql.ds.PGPoolingDataSource;

import java.net.InetSocketAddress;

/**
 * Created by apavlov on 07.03.17.
 */
public class ServerNodeImpl implements ReqDispatcher {
    private final PGPoolingDataSource ds;

    public ServerNodeImpl(final PGPoolingDataSource ds) {
        this.ds = ds;
    }

    @Override
    public void process(Kad2Ping p, InetSocketAddress address) {
        Kad2Pong pong = new Kad2Pong();
    }

    @Override
    public void process(Kad2HelloReq p, InetSocketAddress address) {

    }

    @Override
    public void process(Kad2SearchNotesReq p, InetSocketAddress address) {

    }

    @Override
    public void process(Kad2Req p, InetSocketAddress address) {

    }

    @Override
    public void process(Kad2BootstrapReq p, InetSocketAddress address) {

    }

    @Override
    public void process(Kad2PublishKeysReq p, InetSocketAddress address) {

    }

    @Override
    public void process(Kad2PublishSourcesReq p, InetSocketAddress address) {

    }

    @Override
    public void process(Kad2FirewalledReq p, InetSocketAddress address) {

    }

    @Override
    public void process(Kad2SearchKeysReq p, InetSocketAddress address) {

    }

    @Override
    public void process(Kad2SearchSourcesReq p, InetSocketAddress address) {

    }
}
