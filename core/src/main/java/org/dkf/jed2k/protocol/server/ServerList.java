package org.dkf.jed2k.protocol.server;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.*;

import java.nio.ByteBuffer;

import static org.dkf.jed2k.protocol.Unsigned.uint8;

public class ServerList extends Container<UInt8, Endpoint> implements Dispatchable {

    public ServerList() {
        super(uint8(), Endpoint.class);
    }

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return dst;
    }

    @Override
    public int bytesCount() {
        return 0;
    }

    @Override
    public void dispatch(Dispatcher dispatcher) throws JED2KException {
        dispatcher.onServerList(this);
    }
}
