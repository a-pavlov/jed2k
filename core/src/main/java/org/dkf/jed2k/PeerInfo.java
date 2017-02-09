package org.dkf.jed2k;

import lombok.Data;
import lombok.ToString;
import org.dkf.jed2k.protocol.BitField;
import org.dkf.jed2k.protocol.Endpoint;

/**
 * Created by inkpot on 25.08.2016.
 */
@ToString
@Data
public class PeerInfo {
    public static final byte INCOMING = 0x1;
    public static final byte SERVER = 0x2;
    public static final byte DHT = 0x4;
    public static final byte RESUME = 0x8;

    private int downloadSpeed   = 0;
    private int payloadDownloadSpeed = 0;
    private long downloadPayload    = 0;
    private long downloadProtocol   = 0;
    private BitField remotePieces;
    private int failCount   = 0;
    private Endpoint endpoint;
    private String modName;
    private int version;
    private int modVersion;
    private String strModVersion;
    private int sourceFlag;
}
