package org.dkf.jed2k.alert;

import org.dkf.jed2k.protocol.server.ServerInfo;

/**
 * Created by inkpot on 02.09.2016.
 */
public class ServerInfoAlert extends ServerAlert {
    public final ServerInfo info;

    public ServerInfoAlert(final String id, final ServerInfo info) {
        super(id);
        this.info = info;
    }
}
