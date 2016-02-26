package org.jed2k.protocol.server;

import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.Dispatchable;
import org.jed2k.protocol.Dispatcher;

public class ServerInfo extends UsualPacket implements Dispatchable {

    @Override
    public void dispatch(Dispatcher dispatcher) throws JED2KException {
        dispatcher.onServerInfo(this);
    }
}
