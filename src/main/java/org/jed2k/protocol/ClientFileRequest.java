package org.jed2k.protocol;

import org.jed2k.exception.JED2KException;

public class ClientFileRequest extends Hash implements Dispatchable {
    public ClientFileRequest(Hash h) {
        super(h);
    }

    @Override
    public void dispatch(Dispatcher dispatcher) throws JED2KException {
        dispatcher.onClientFileRequest(this);
    }
}
