package org.jed2k.protocol;

import org.jed2k.exception.JED2KException;

public class ClientOutOfParts extends WithoutDataPacket implements Dispatchable {

    @Override
    public void dispatch(Dispatcher dispatcher) throws JED2KException {
        dispatcher.onClientOutOfParts(this);        
    }
}
