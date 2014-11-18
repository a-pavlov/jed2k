package org.jed2k.protocol;

import org.jed2k.exception.JED2KException;

public class ClientFileStatusRequest extends Hash implements Dispatchable {

    public ClientFileStatusRequest(Hash hash) {
        super(hash);
    }
    
    public ClientFileStatusRequest() {
        super();
    }
    
    @Override
    public void dispatch(Dispatcher dispatcher) throws JED2KException {
        dispatcher.onClientFileStatusRequest(this);
    }
    
}
