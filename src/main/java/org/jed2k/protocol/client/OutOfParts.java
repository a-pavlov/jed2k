package org.jed2k.protocol.client;

import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.Dispatchable;
import org.jed2k.protocol.Dispatcher;
import org.jed2k.protocol.WithoutDataPacket;

public class OutOfParts extends WithoutDataPacket implements Dispatchable {

    @Override
    public void dispatch(Dispatcher dispatcher) throws JED2KException {
        dispatcher.onClientOutOfParts(this);        
    }
}
