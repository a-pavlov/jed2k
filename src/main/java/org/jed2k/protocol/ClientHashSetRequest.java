package org.jed2k.protocol;

import org.jed2k.exception.JED2KException;

public class ClientHashSetRequest extends Hash implements Dispatchable {

    public ClientHashSetRequest(Hash hash) {
        super(hash);
    }
    
    public ClientHashSetRequest() {
        super();
    }
    
    @Override
    public void dispatch(Dispatcher dispatcher) throws JED2KException {
        dispatcher.onClientHashSetRequest(this);
    }   
}
