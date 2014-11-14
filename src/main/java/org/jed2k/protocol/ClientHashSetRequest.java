package org.jed2k.protocol;

import org.jed2k.exception.JED2KException;

public class ClientHashSetRequest extends Hash implements Dispatchable {

    @Override
    public void dispatch(Dispatcher dispatcher) throws JED2KException {
        dispatcher.onClientHashSetRequest(this);
    }   
}
