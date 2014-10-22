package org.jed2k;

import org.jed2k.protocol.Hash;

public class Transfer {
    private Hash fileHash;
    
    public Transfer(Hash hash) {
        this.fileHash = hash;
    }
    
    public Hash fileHash() {
        return fileHash;
    }
    
    public void append(PeerConnection connection) {
        
    }
}
