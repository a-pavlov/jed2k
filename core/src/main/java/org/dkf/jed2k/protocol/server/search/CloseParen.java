package org.dkf.jed2k.protocol.server.search;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Serializable;

import java.nio.ByteBuffer;

public class CloseParen implements Serializable {

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        assert(false);
        return null;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        assert(false);
        return null;
    }

    @Override
    public int bytesCount() {
        assert(false);
        return 0;
    }

    @Override
    public String toString() {
        return ")";
    }

}
