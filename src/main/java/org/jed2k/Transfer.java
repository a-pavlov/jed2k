package org.jed2k;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.jed2k.protocol.Hash;
import org.jed2k.protocol.NetworkIdentifier;


public class Transfer {
    private Hash fileHash;
    private Set<NetworkIdentifier> sources = new TreeSet<NetworkIdentifier>();
    
    public Transfer(Hash hash) {
        this.fileHash = hash;
    }
    
    public Hash fileHash() {
        return fileHash;
    }
    
    public void append(PeerConnection connection) {
        
    }
    
    void setupSources(Collection<NetworkIdentifier> sources) {
        for(NetworkIdentifier entry: sources) {
            if (!this.sources.contains(entry)) {
                this.sources.add(entry);
                // process new source
            }
        }
    }
}
