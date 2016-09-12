package org.dkf.jed2k;

import org.dkf.jed2k.protocol.BitField;

/**
 * class contains information about whole transfer status at the moment
 */
public class TransferStatus {

    /**
     * all available transfer states
     */
    public enum TransferState {
        LOADING_RESUME_DATA,
        DOWNLOADING,
        FINISHED
    };

    public boolean paused;
    public float progress   = 0f;
    public int progressPPM          = 0;
    public long downloadPayload     = 0;
    public long downloadProtocol    = 0;
    public int downloadRate         = 0;
    public int downloadPayloadRate  = 0;
    public int numPeers             = 0;
    public long totalDone           = 0;
    public long totalWanted         = 0;
    public long eta                 = 0;
    public BitField pieces;
    public int numPieces            = 0;
}
