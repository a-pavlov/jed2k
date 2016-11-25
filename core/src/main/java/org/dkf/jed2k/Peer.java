package org.dkf.jed2k;

import org.dkf.jed2k.protocol.Endpoint;

/**
 * Created by ap197_000 on 04.07.2016.
 * information about peer
 */
public class Peer implements Comparable<Peer> {

    public enum SourceFlag {
        SF_SERVER(0x1),
        SF_INCOMING(0x2),
        SF_DHT(0x4),
        SF_RESUME_DATA(0x8);

        public int value;

        SourceFlag(int s) {
            this.value = s;
        }
    }

    long    lastConnected   = 0;
    long    nextConnection  = 0;
    int     failCount       = 0;
    boolean connectable     = false;
    int source      = 0;
    Endpoint endpoint;
    private PeerConnection  connection = null;

    public Peer(Endpoint ep) {
        endpoint = ep;
    }

    public Peer(Endpoint ep, boolean conn) {
        endpoint = ep;
        connectable = conn;
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

    public boolean isConnectable() {
        return connectable;
    }

    PeerConnection getConnection() { return connection; }

    void setConnection(final PeerConnection c) {
        connection = c;
    }

    boolean hasConnection() {
        return connection != null;
    }

    @Override
    public String toString() {
        return "peer: " + endpoint + " "
                + (hasConnection()?"connected ":"not connected ")
                + (connectable?"connectable":"notconnectable")
                + " fail count {" + failCount + "}"
                + " last connected: " + lastConnected;
    }


    public void setFailCount(int failCount) {
        assert failCount > 0;
        this.failCount = failCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public void setConnectable(boolean connectable) {
        this.connectable = connectable;
    }

    public boolean getConnectable() {
        return connectable;
    }
}
