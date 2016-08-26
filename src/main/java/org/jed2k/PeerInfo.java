package org.jed2k;

import org.jed2k.protocol.BitField;
import org.jed2k.protocol.NetworkIdentifier;

/**
 * Created by inkpot on 25.08.2016.
 */
public class PeerInfo {
    int downloadSpeed;
    int payloadDownloadSpeed;
    long downloadPayload;
    long downloadProtocol;
    BitField remotePieces;
    int failCount;
    NetworkIdentifier endpoint;
    String modName;
    int version;
    int modVersion;
}
