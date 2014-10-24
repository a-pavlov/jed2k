package org.jed2k.protocol;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import org.jed2k.exception.JED2KException;
import static org.jed2k.protocol.Unsigned.uint32;
import static org.jed2k.Utils.sizeof;

public class SearchResult extends SoftSerializable implements Dispatchable {
    public final ContainerHolder<UInt32, SharedFileEntry> files = ContainerHolder.make32(new LinkedList<SharedFileEntry>(), SharedFileEntry.class);
    public byte moreResults = 0;
    
    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        files.get(src);
        moreResults = src.get();        
        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        files.put(dst);
        return dst.put(moreResults);       
    }

    @Override
    public int bytesCount() {
        return files.bytesCount() + sizeof(moreResults);        
    }

    @Override
    public ByteBuffer get(ByteBuffer src, int limit) throws JED2KException {
        files.get(src);
        if (limit - files.bytesCount() > 0) moreResults = src.get();
        return src;        
    }
    
    @Override
    public String toString() {
        return files.toString() + ((moreResults==(byte)0)?"false":"true");
    }

    @Override
    public void dispatch(Dispatcher dispatcher) throws JED2KException {
        dispatcher.onSearchResult(this);
    }
}
