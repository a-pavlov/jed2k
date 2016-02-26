package org.jed2k.protocol.server;

import static org.jed2k.protocol.Unsigned.uint16;

import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.ByteContainer;
import org.jed2k.protocol.Dispatchable;
import org.jed2k.protocol.Dispatcher;
import org.jed2k.protocol.UInt16;

public class Message extends ByteContainer<UInt16> implements Dispatchable {
    public Message() {
        super(uint16());
    }

    @Override
    public void dispatch(Dispatcher dispatcher) throws JED2KException {
        dispatcher.onServerMessage(this);
    }
}
