package org.dkf.jed2k.protocol.server;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Hash;
import org.dkf.jed2k.protocol.NetworkIdentifier;
import org.dkf.jed2k.protocol.Serializable;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 05.11.2016.
 */
public class FileSourceObfu implements Serializable {
    public NetworkIdentifier endpoint = new NetworkIdentifier();
    byte cryptOptions;
    public Hash hash = Hash.INVALID;

    private boolean hasHash() {
        return ((cryptOptions & 0x80) != 0);
    }

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        cryptOptions = endpoint.get(src).get();
        if (hasHash()) hash.get(src);
        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        assert false;
        return null;
    }

    @Override
    public int bytesCount() {
        return endpoint.bytesCount() + 1 + (hasHash()?hash.bytesCount():0);
    }
}
