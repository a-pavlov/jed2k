package org.dkf.jed2k.protocol.server.search;

import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.*;
import org.dkf.jed2k.protocol.server.SharedFileEntry;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import static org.dkf.jed2k.Utils.sizeof;

public class SearchResult extends SoftSerializable implements Dispatchable {
    private final Container<UInt32, SharedFileEntry> results = Container.makeInt(SharedFileEntry.class);
    private byte moreResults = 0;

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        results.get(src);
        try {
            moreResults = src.get();
        } catch(BufferUnderflowException e) {
            throw new JED2KException(ErrorCode.BUFFER_UNDERFLOW_EXCEPTION);
        } catch(Exception e) {
            throw new JED2KException(ErrorCode.BUFFER_GET_EXCEPTION);
        }
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
        try {
            if (limit - results.bytesCount() > 0) moreResults = src.get();
        } catch(BufferUnderflowException e) {
            throw new JED2KException(ErrorCode.BUFFER_UNDERFLOW_EXCEPTION);
        } catch(Exception e) {
            throw new JED2KException(ErrorCode.BUFFER_GET_EXCEPTION);
        }
        return src;
    }

    @Override
    public void dispatch(Dispatcher dispatcher) throws JED2KException {
        dispatcher.onSearchResult(this);
    }

    public boolean hasMoreResults() {
        return moreResults != 0;
    }

    public Container<UInt32, SharedFileEntry> getResults() {
        return this.results;
    }

    public byte getMoreResults() {
        return this.moreResults;
    }

    public String toString() {
        return "SearchResult(results=" + this.getResults() + ", moreResults=" + this.getMoreResults() + ")";
    }
}
