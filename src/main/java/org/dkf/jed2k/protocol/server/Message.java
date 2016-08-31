package org.dkf.jed2k.protocol.server;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.ByteContainer;
import org.dkf.jed2k.protocol.Dispatchable;
import org.dkf.jed2k.protocol.Dispatcher;
import org.dkf.jed2k.protocol.UInt16;

import static org.dkf.jed2k.protocol.Unsigned.uint16;

public class Message extends ByteContainer<UInt16> implements Dispatchable {
    public Message() {
        super(uint16());
    }

    @Override
    public void dispatch(Dispatcher dispatcher) throws JED2KException {
        dispatcher.onServerMessage(this);
    }
}
