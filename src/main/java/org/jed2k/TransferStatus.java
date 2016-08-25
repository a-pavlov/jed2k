package org.jed2k;

import org.jed2k.protocol.BitField;

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
    public int progressPPM;
    public long downloadPayload;
    public long downloadProtocol;
    public int downloadRate;
    public int downloadPayloadRate;
    public int numPeers;
    public long totalDone;
    public long totalWanted;
    public BitField pieces;
    public int numPieces;
}
