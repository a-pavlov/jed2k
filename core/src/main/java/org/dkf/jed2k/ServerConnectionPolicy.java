package org.dkf.jed2k;

import java.net.InetSocketAddress;

public class ServerConnectionPolicy {
    private final int reconnectSecondsTimeout;
    private final int maxReconnects;

    private int iteration;
    private String identifier;
    private InetSocketAddress address;
    private long nextConnectTime = -1;

    public ServerConnectionPolicy(int reconnectSecondsTimeout, int maxReconnects) {
        assert reconnectSecondsTimeout >= 0;
        this.reconnectSecondsTimeout = reconnectSecondsTimeout;
        this.maxReconnects = maxReconnects;
        removeConnectCandidates();
    }

    public void setServerConnectionFailed(String identifier, InetSocketAddress address, long currentSessionTime) {
        assert identifier != null;
        assert address != null;
        if (this.identifier == null || !this.identifier.equals(identifier)) {
            iteration = 0;
            this.identifier = identifier;
            this.address = address;
        } else {
            iteration++;
        }

        nextConnectTime = (hasCandidate() && hasIterations())?currentSessionTime + iteration*reconnectSecondsTimeout*1000:-1;
    }

    public boolean hasCandidate() {
        return identifier != null && address != null;
    }

    public boolean hasIterations() {
        return iteration < maxReconnects;
    }

    public void removeConnectCandidates() {
        iteration = maxReconnects;
        identifier = null;
        address = null;
        nextConnectTime = -1;
    }

    public Pair<String, InetSocketAddress> getConnectCandidate(long currentSessionTime) {
        if (nextConnectTime != -1 && nextConnectTime < currentSessionTime) {
            assert identifier != null;
            assert address != null;
            return Pair.make(identifier, address);
        }

        return null;
    }
}
