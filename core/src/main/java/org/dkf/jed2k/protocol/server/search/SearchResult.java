package org.dkf.jed2k.protocol.server.search;

import lombok.Getter;
import lombok.ToString;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.*;
import org.dkf.jed2k.protocol.server.SharedFileEntry;

import java.nio.ByteBuffer;

import static org.dkf.jed2k.Utils.sizeof;

@Getter
@ToString
public class SearchResult extends SoftSerializable implements Dispatchable {
    private final Container<UInt32, SharedFileEntry> results = Container.makeInt(SharedFileEntry.class);
    private byte moreResults = 0;

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        results.get(src);
        moreResults = src.get();
        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        results.put(dst);
        return dst.put(moreResults);
    }

    @Override
    public int bytesCount() {
        return results.bytesCount() + sizeof(moreResults);
    }

    @Override
    public ByteBuffer get(ByteBuffer src, int limit) throws JED2KException {
        results.get(src);
        if (limit - results.bytesCount() > 0) moreResults = src.get();
        return src;
    }

    @Override
    public void dispatch(Dispatcher dispatcher) throws JED2KException {
        dispatcher.onSearchResult(this);
    }

    public boolean hasMoreResults() {
        return moreResults != 0;
    }
}
