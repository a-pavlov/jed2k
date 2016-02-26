package org.jed2k.protocol.client;

import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.Dispatchable;
import org.jed2k.protocol.Dispatcher;

public class ExtHello extends ExtendedHandshake implements Dispatchable {

    @Override
    public void dispatch(Dispatcher dispatcher) throws JED2KException {
        dispatcher.onClientExtHello(this);        
    }
}
