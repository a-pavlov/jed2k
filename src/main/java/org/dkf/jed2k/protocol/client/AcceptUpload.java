package org.dkf.jed2k.protocol.client;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Dispatchable;
import org.dkf.jed2k.protocol.Dispatcher;
import org.dkf.jed2k.protocol.WithoutDataPacket;

public class AcceptUpload extends WithoutDataPacket implements Dispatchable {
    @Override
    public void dispatch(Dispatcher dispatcher) throws JED2KException {
        dispatcher.onAcceptUpload(this);
    }
}
