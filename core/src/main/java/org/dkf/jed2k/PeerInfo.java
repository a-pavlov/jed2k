package org.dkf.jed2k;

import lombok.ToString;
import org.dkf.jed2k.protocol.BitField;
import org.dkf.jed2k.protocol.Endpoint;

/**
 * Created by inkpot on 25.08.2016.
 */
@ToString
public class PeerInfo {
    public int downloadSpeed   = 0;
    public int payloadDownloadSpeed = 0;
    public long downloadPayload    = 0;
    public long downloadProtocol   = 0;
    public BitField remotePieces;
    public int failCount   = 0;
    public Endpoint endpoint;
    public String modName;
    public int version;
    public int modVersion;
    public String strModVersion;
    public int sourceFlag;
}
