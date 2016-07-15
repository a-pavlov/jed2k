package org.jed2k;

/**
 * Created by inkpot on 15.07.2016.
 */
public class PeerRequest {
    public int piece    = -1;
    public int offset   = -1;
    public int length   = -1;

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof PeerRequest) {
            return ((PeerRequest)o).piece == piece &&
                    ((PeerRequest)o).offset == offset &&
                    ((PeerRequest)o).length == length;
        }

        return false;
    }
}
