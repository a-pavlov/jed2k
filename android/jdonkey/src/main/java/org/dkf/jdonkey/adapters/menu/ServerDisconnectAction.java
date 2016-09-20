package org.dkf.jdonkey.adapters.menu;

import android.content.Context;
import org.dkf.jdonkey.Engine;
import org.dkf.jdonkey.R;
import org.dkf.jdonkey.views.MenuAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ap197_000 on 13.09.2016.
 */
public class ServerDisconnectAction extends MenuAction {
    private final Logger log = LoggerFactory.getLogger(ServerDisconnectAction.class);
    private final String host;
    private int port;
    private String serverId;

    public ServerDisconnectAction(Context context, final String host, int port, final String serverId) {
        super(context, R.drawable.ic_power_settings_new_black_24dp, R.string.server_disconnect_action, host);
        this.host = host;
        this.port = port;
        this.serverId = serverId;
    }

    @Override
    protected void onClick(Context context) {
        log.info("disconnect from {} {}", host, port);
        Engine.instance().disconnectFrom();
    }
}
