package org.dkf.jed2k.alert;

import org.dkf.jed2k.protocol.server.ServerInfo;

/**
 * Created by inkpot on 02.09.2016.
 */
public class ServerInfoAlert extends Alert {

    public final ServerInfo info;

    public ServerInfoAlert(final ServerInfo info) {
        this.info = info;
    }

    @Override
    public Severity severity() {
        return null;
    }

    @Override
    public int category() {
        return 0;
    }
}
