package org.jed2k.protocol;

public class ServerInfo extends ServerUsualPacket implements Dispatchable {

    @Override
    public boolean dispatch(Dispatcher dispatcher) {
        return dispatcher.onServerInfo(this);
    }
}
