package org.jed2k.protocol;

import org.jed2k.exception.JED2KException;

public class ClientExtHelloAnswer extends ClientExtendedHandshake implements Dispatchable {

    @Override
    public void dispatch(Dispatcher dispatcher) throws JED2KException {
        dispatcher.onClientExtHelloAnswer(this);        
    }
    
}
