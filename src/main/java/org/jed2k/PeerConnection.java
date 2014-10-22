package org.jed2k;

import org.jed2k.protocol.NetworkIdentifier;

public class PeerConnection {
    NetworkIdentifier point;
    
    public boolean hasPort() {
        return false;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof PeerConnection) {
            if (((PeerConnection)o).hasPort() && hasPort()) {
                return point.equals(((PeerConnection) o).point);
            } else {
                return point.ip() == ((PeerConnection)o).point.ip(); 
            }
        }
        
        return false;
    }
}
