package org.jed2k.protocol.client;

import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.Dispatchable;
import org.jed2k.protocol.Dispatcher;
import org.jed2k.protocol.Hash;

public class HashSetRequest extends Hash implements Dispatchable {

    public HashSetRequest(Hash hash) {
        super(hash);
    }

    public HashSetRequest() {
        super();
    }

    @Override
    public void dispatch(Dispatcher dispatcher) throws JED2KException {
        dispatcher.onClientHashSetRequest(this);
    }

    @Override
    public String toString() {
        return String.format("HashSetRequest %s", super.toString());
    }
}
