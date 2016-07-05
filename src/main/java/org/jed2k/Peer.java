package org.jed2k;

import org.jed2k.protocol.NetworkIdentifier;

/**
 * Created by ap197_000 on 04.07.2016.
 * information about peer
 */
public class Peer implements Comparable<Peer> {
    long    lastConnected   = 0;
    long    nextConnection  = 0;
    long    failCount       = 0;
    NetworkIdentifier   endpoint;
    PeerConnection  connection = null;

    public Peer(NetworkIdentifier ep) {
        endpoint = ep;
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
}
