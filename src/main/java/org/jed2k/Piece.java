package org.jed2k;

import org.jed2k.protocol.Hash;
import java.util.ArrayList;

public class Piece {
    private Hash hash = Hash.INVALID;
    private ArrayList<Hash> blocks;
    private int emptyBlocks;
    
    public Piece(int blocksCount) {
        assert(blocksCount > 0);
        blocks = new ArrayList<Hash>(blocksCount);
        for(int i = 0; i < blocksCount; ++i) {
            blocks.add(Hash.INVALID);
        }
        
        emptyBlocks = blocks.size();
    }
    
    public void setupPieceHash(Hash h) {
        this.hash.assign(h);
    }
    
    public void setBlockHash(int index, Hash h) {
        assert(index < blocks.size());
        blocks.get(index).assign(h);
        --emptyBlocks;
        assert(emptyBlocks >= 0);
        
        if (emptyBlocks == 0) {
            // verify hash
            Hash localHash = Hash.fromHashSet(blocks);
            if (!localHash.equals(hash)) {
                // we have incorrect data in some block! - handle this situation 
            }
        }
    }
    
    public boolean finalized() {
        return (emptyBlocks == 0);
    }
}
