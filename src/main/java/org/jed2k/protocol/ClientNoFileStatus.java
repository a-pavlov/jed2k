package org.jed2k.protocol;

import org.jed2k.exception.JED2KException;

public class ClientNoFileStatus extends Hash implements Dispatchable {

    public ClientNoFileStatus(Hash hash) {
        super(hash);
    }
    
    public ClientNoFileStatus() {
        super();
    }
    
    @Override
    public void dispatch(Dispatcher dispatcher) throws JED2KException {
        dispatcher.onClientNoFileStatus(this);        
    }
    
}
