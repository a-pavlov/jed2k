package org.jed2k;

import org.jed2k.protocol.NetworkIdentifier;

/**
 * Created by ap197_000 on 04.07.2016.
 * information about peer
 */
public class Peer {
    long    lastConnected   = 0;
    long    nextConnection  = 0;
    long    failCount       = 0;
    NetworkIdentifier   endpoint;
    PeerConnection  connection = null;

    Peer(NetworkIdentifier ep) {
        endpoint = ep;
    }
}
