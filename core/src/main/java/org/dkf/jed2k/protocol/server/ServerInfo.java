package org.dkf.jed2k.protocol.server;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Dispatchable;
import org.dkf.jed2k.protocol.Dispatcher;

public class ServerInfo extends UsualPacket implements Dispatchable {

    @Override
    public void dispatch(Dispatcher dispatcher) throws JED2KException {
        dispatcher.onServerInfo(this);
    }
}
