package org.jed2k.disk;

import java.nio.ByteBuffer;

import org.jed2k.PieceBlock;
import org.jed2k.protocol.Hash;

public class DiskIOManager {
    private String filename;
    private Hash hash;
    private long size;
    
    public DiskIOManager(String filename, Hash hash, long size) {
        this.filename = filename;
        this.hash = hash;
        this.size = size;
    }
    
    public BlockStatus getBlockStatus(PieceBlock block) {
        return BlockStatus.Empty;
    }
    
    public void setBlockStatus(PieceBlock block, BlockStatus status) {
        
    }
    
    public ByteBuffer allocateBuffer(PieceBlock block) {
        return null;
    }
}
