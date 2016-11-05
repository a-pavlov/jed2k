package org.dkf.jed2k.protocol.server;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.*;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 05.11.2016.
 */
public class FoundFileSourcesObfu implements Serializable, Dispatchable {
    public Hash hash = Hash.INVALID;
    public Container<UInt8, FileSourceObfu> sources = Container.makeByte(FileSourceObfu.class);

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

    @Override
    public void dispatch(Dispatcher dispatcher) throws JED2KException {
        dispatcher.onFoundFileSourcesObfu(this);
    }
}
