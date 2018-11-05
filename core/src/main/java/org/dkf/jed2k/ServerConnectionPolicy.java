package org.dkf.jed2k;

import java.net.InetSocketAddress;

public class ServerConnectionPolicy {
    private static final int MAX_RETRY_COUNT = 100;
    private final int reconnectSecondsTimeout;
    private final int maxReconnects;
    private int iteration   = MAX_RETRY_COUNT;
    private String identifier;
    private InetSocketAddress address;
    private long nextConnectTime = -1;

    public ServerConnectionPolicy(int reconnectSecondsTimeout, int maxReconnects) {
        assert reconnectSecondsTimeout >= 0;
        assert maxReconnects < MAX_RETRY_COUNT;
        this.reconnectSecondsTimeout = reconnectSecondsTimeout;
        this.maxReconnects = maxReconnects;
    }

    public void setServerConnectionFailed(String identifier, InetSocketAddress address, long currentSessionTime) {
        if (this.identifier == null || !this.identifier.equals(identifier)) {
            iteration = 0;
            this.identifier = identifier;
            this.address = address;
        } else {
            iteration++;
        }

        nextConnectTime = (iteration >= maxReconnects)?-1:currentSessionTime + iteration*reconnectSecondsTimeout*1000;
    }

    public void removeConnectCandidates() {
        iteration = MAX_RETRY_COUNT;
        identifier = null;
        address = null;
    }

    public Pair<String, InetSocketAddress> getConnectCandidate(long currentSessionTime) {
        return (nextConnectTime != -1 && nextConnectTime < currentSessionTime)?Pair.make(identifier, address):null;
    }
}
