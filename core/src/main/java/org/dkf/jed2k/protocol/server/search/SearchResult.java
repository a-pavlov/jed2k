package org.dkf.jed2k.protocol.server.search;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.*;
import org.dkf.jed2k.protocol.server.SharedFileEntry;

import java.nio.ByteBuffer;

import static org.dkf.jed2k.Utils.sizeof;

public class SearchResult extends SoftSerializable implements Dispatchable {
    public final Container<UInt32, SharedFileEntry> files = Container.makeInt(SharedFileEntry.class);
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

    public boolean hasMoreResults() {
        return moreResults != 0;
    }
}
