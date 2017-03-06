package org.dkf.jed2k.kad.server;

import java.net.InetSocketAddress;

/**
 * Created by apavlov on 06.03.17.
 */
public abstract class DhtRequestHandler implements Runnable {
    private final InetSocketAddress originator;
    private final byte[] buffer;

    public DhtRequestHandler(final InetSocketAddress originator, final byte[] buffer) {
        this.originator = originator;
        this.buffer = buffer;
    }
}
