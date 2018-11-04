package org.dkf.jed2k;

import java.net.InetSocketAddress;

public class ServerConnectionPolicy {
    private final int reconnectSecondsTimeout;
    private final int maxReconnects;
    private int iteration   = 0;
    private String identifier;
    private InetSocketAddress address;
    private long nextConnectTime = 0;

    public ServerConnectionPolicy(int reconnectSecondsTimeout, int maxReconnects) {
        assert reconnectSecondsTimeout >= 0;
        this.reconnectSecondsTimeout = reconnectSecondsTimeout;
        this.maxReconnects = maxReconnects;
    }

    public void setServerConnection(String identifier, InetSocketAddress address, long currentSessionTime) {
        if (this.identifier == null || !this.identifier.equals(identifier)) {
            iteration = 1;
            this.identifier = identifier;
            this.address = address;
        } else {
            iteration++;
        }

        nextConnectTime = (iteration > maxReconnects)?0:currentSessionTime + iteration*reconnectSecondsTimeout*1000;
    }

    public void clear() {
        iteration = 0;
        identifier = null;
        address = null;
    }

    public Pair<String, InetSocketAddress> getConnectCandidate(long currentSessionTime) {
        return (identifier != null && nextConnectTime != 0 && nextConnectTime < currentSessionTime)?Pair.make(identifier, address):null;
    }
}
