package org.jed2k.protocol;

import static org.jed2k.protocol.Unsigned.uint16;

public class ServerMessage extends ByteContainer<UInt16> implements Dispatchable {
    public ServerMessage() {
        super(uint16());
    }

    @Override
    public boolean dispatch(Dispatcher dispatcher) {
        return dispatcher.onServerMessage(this);
    }
}
