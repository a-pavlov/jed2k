package org.dkf.jed2k;

import org.dkf.jed2k.protocol.Hash;

public class Settings {
    public Hash userAgent = new Hash(Hash.LIBED2K);
    public String modName = "jed2k";
    public String clientName = "jed2k";
    public int listenPort = 4661;
    public int udpPort = 4662;
    public int version = 0x3c;
    public int modMajor = 0;
    public int modMinor = 0;
    public int modBuild = 0;
    public int maxFailCount = 20;
    public int maxPeerListSize = 100;
    public int minPeerReconnectTime = 10;
    public int peerConnectionTimeout = 5;
    public int sessionConnectionsLimit = 20;
    public int bufferPoolSize = 250;    // dataSize of buffer pool in blocks of 180K
    public int maxConnectionsPerSecond = 10;    // for testing purposes
    public int compressionVersion = 0;  // use 1 for activate compression
    public int serverSearchTimeout = 15;    // seconds

    /**
     * send ping message to server every serverPingTimeout seconds
     */
    public long serverPingTimeout = 0;

    @Override
    public String toString() {
        return "Settings{" +
                "userAgent=" + userAgent +
                ", modName='" + modName + '\'' +
                ", clientName='" + clientName + '\'' +
                ", listenPort=" + listenPort +
                ", udpPort=" + udpPort +
                ", version=" + version +
                ", modMajor=" + modMajor +
                ", modMinor=" + modMinor +
                ", modBuild=" + modBuild +
                ", maxFailCount=" + maxFailCount +
                ", maxPeerListSize=" + maxPeerListSize +
                ", minPeerReconnectTime=" + minPeerReconnectTime +
                ", peerConnectionTimeout=" + peerConnectionTimeout +
                ", sessionConnectionsLimit=" + sessionConnectionsLimit +
                ", bufferPoolSize=" + bufferPoolSize +
                ", maxConnectionsPerSecond=" + maxConnectionsPerSecond +
                ", compressionVersion=" + compressionVersion +
                ", serverSearchTimeout=" + serverSearchTimeout +
                ", serverPingTimeout=" + serverPingTimeout +
                '}';
    }
}
