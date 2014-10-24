package org.jed2k.protocol;

import static org.jed2k.protocol.Unsigned.uint16;

import org.jed2k.exception.JED2KException;

public class ServerMessage extends ByteContainer<UInt16> implements Dispatchable {
    public ServerMessage() {
        super(uint16());
    }

    @Override
    public void dispatch(Dispatcher dispatcher) throws JED2KException {
        dispatcher.onServerMessage(this);
    }
}
