package org.jed2k;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.jed2k.protocol.Hash;
import org.jed2k.protocol.NetworkIdentifier;
import org.jed2k.data.PieceBlock;

public class Transfer implements Tickable {
    private Hash fileHash;    
    private Set<NetworkIdentifier> sources = new TreeSet<NetworkIdentifier>();
    private long size;
    private ArrayList<Hash> hashset;
    
    public Transfer(Hash hash, long size) {
        this.fileHash = hash;
        this.size = size;
        this.hashset = null;
    }
    
    public Hash fileHash() {
        return fileHash;
    }
    
    public long fileSize() {
        return size;
    }
    
    public boolean validateHashset(Collection<Hash> hashset) {
        if (this.hashset != null) {
            // compare
        } else {            
            return fileHash.equals(Hash.fromHashSet(hashset)); 
        }
        
        return true;
    }
    
    public PieceBlock requestBlock() {
        return new PieceBlock(0,0);
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

	@Override
	public void secondTick(long tick_interval_ms) {
		// TODO Auto-generated method stub
		
	}
}
