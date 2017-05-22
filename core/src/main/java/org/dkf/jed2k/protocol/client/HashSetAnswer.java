package org.dkf.jed2k.protocol.client;

import lombok.Data;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.*;

import java.nio.ByteBuffer;

@Data
public class HashSetAnswer implements Serializable, Dispatchable {
    private final Hash hash = new Hash();
    private final Container<UInt16, Hash> parts = Container.makeShort(Hash.class);

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return parts.get(hash.get(src));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return parts.put(hash.put(dst));
    }

    @Override
    public int bytesCount() {
        return hash.bytesCount() + parts.bytesCount();
    }

    @Override
    public void dispatch(Dispatcher dispatcher) throws JED2KException {
        dispatcher.onClientHashSetAnswer(this);
    }
}
