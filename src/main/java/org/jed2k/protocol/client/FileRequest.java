package org.jed2k.protocol.client;

import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.Dispatchable;
import org.jed2k.protocol.Dispatcher;
import org.jed2k.protocol.Hash;

public class FileRequest extends Hash implements Dispatchable {
    public FileRequest(Hash h) {
        super(h);
    }

    @Override
    public void dispatch(Dispatcher dispatcher) throws JED2KException {
        dispatcher.onClientFileRequest(this);
    }
}
