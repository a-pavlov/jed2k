package org.dkf.jed2k;

import org.dkf.jed2k.protocol.Endpoint;

/**
 * Created by inkpot on 04.07.2016.
 * information about peer
 */
public class Peer implements Comparable<Peer> {
    private long    lastConnected   = 0;
    private long    nextConnection  = 0;
    private int     failCount       = 0;
    public boolean connectable     = false;
    private int     sourceFlag      = 0;
    private PeerConnection  connection = null;
    private final Endpoint        endpoint;

    public Peer(Endpoint ep) {
        assert ep != null;
        endpoint = ep;
    }

    public Peer(Endpoint ep, boolean conn, int sourceFlag) {
        assert ep != null;
        this.endpoint = ep;
        this.connectable = conn;
        this.sourceFlag = sourceFlag;
    }

    @Override
    public int compareTo(Peer o) {
        return endpoint.compareTo(o.endpoint);
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof Peer) {
            Peer other = (Peer)o;
            return endpoint.equals(other.endpoint);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return endpoint.hashCode();
    }

    boolean hasConnection() {
        return connection != null;
    }

    public long getLastConnected() {
        return this.lastConnected;
    }

    public long getNextConnection() {
        return this.nextConnection;
    }

    public int getFailCount() {
        return this.failCount;
    }

    public boolean isConnectable() {
        return this.connectable;
    }

    public int getSourceFlag() {
        return this.sourceFlag;
    }

    public PeerConnection getConnection() {
        return this.connection;
    }

    public Endpoint getEndpoint() {
        return this.endpoint;
    }

    public void setLastConnected(long lastConnected) {
        this.lastConnected = lastConnected;
    }

    public void setNextConnection(long nextConnection) {
        this.nextConnection = nextConnection;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    public void setConnectable(boolean connectable) {
        this.connectable = connectable;
    }

    public void setSourceFlag(int sourceFlag) {
        this.sourceFlag = sourceFlag;
    }

    public void setConnection(PeerConnection connection) {
        this.connection = connection;
    }

    public String toString() {
        return "Peer(lastConnected=" + this.getLastConnected() + ", nextConnection=" + this.getNextConnection() + ", failCount=" + this.getFailCount() + ", connectable=" + this.isConnectable() + ", sourceFlag=" + this.getSourceFlag() + ", connection=" + this.getConnection() + ", endpoint=" + this.getEndpoint() + ")";
    }
}
