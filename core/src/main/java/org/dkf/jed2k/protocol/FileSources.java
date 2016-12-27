package org.dkf.jed2k.protocol;

import org.dkf.jed2k.exception.JED2KException;

import java.nio.ByteBuffer;

public class FileSources implements Serializable {
    private Hash hash;
    private Container<UInt8, Endpoint> sources;

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return sources.get(hash.get(src));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return sources.put(hash.put(dst));
    }

    @Override
    public int bytesCount() {
        return hash.bytesCount() + sources.bytesCount();
    }

}