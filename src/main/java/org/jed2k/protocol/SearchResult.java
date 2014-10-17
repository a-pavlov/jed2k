package org.jed2k.protocol;

import java.nio.ByteBuffer;
import java.util.LinkedList;

import static org.jed2k.protocol.Unsigned.uint32;
import static org.jed2k.Utils.sizeof;

public class SearchResult extends SoftSerializable {
    public ContainerHolder<UInt32, SharedFileEntry> files = 
            new ContainerHolder<UInt32, SharedFileEntry>(uint32(), new LinkedList<SharedFileEntry>(), SharedFileEntry.class);
    public byte moreResults = 0;
    
    @Override
    public ByteBuffer get(ByteBuffer src) throws ProtocolException {
        files.get(src);
        moreResults = src.get();        
        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws ProtocolException {
        files.put(dst);
        return dst.put(moreResults);       
    }

    @Override
    public int size() {
        return files.size() + sizeof(moreResults);        
    }

    @Override
    public ByteBuffer get(ByteBuffer src, int limit) throws ProtocolException {
        files.get(src);
        if (limit - files.size() > 0) moreResults = src.get();
        return src;        
    }
}
