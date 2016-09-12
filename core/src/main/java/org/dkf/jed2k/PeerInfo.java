package org.dkf.jed2k;

import org.dkf.jed2k.protocol.BitField;
import org.dkf.jed2k.protocol.NetworkIdentifier;

/**
 * Created by inkpot on 25.08.2016.
 */
public class PeerInfo {
    int downloadSpeed   = 0;
    int payloadDownloadSpeed = 0;
    long downloadPayload    = 0;
    long downloadProtocol   = 0;
    BitField remotePieces;
    int failCount   = 0;
    NetworkIdentifier endpoint;
    String modName;
    int version;
    int modVersion;
}
