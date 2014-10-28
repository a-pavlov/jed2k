package org.jed2k.protocol;

import java.nio.ByteBuffer;

import org.jed2k.exception.JED2KException;

public class ClientFileAnswer implements Serializable {
    public Hash hash = new Hash();
    public ByteContainer<UInt16> name = new ByteContainer<UInt16>(Unsigned.uint16());

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return name.get(hash.get(src));
    }
    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return name.put(hash.put(dst));
    }
    @Override
    public int bytesCount() {
        return hash.bytesCount() + name.bytesCount();
    }
}
