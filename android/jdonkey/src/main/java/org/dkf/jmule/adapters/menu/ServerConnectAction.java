package org.dkf.jmule.adapters.menu;

import android.content.Context;
import org.dkf.jmule.Engine;
import org.dkf.jmule.R;
import org.dkf.jmule.views.MenuAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ap197_000 on 13.09.2016.
 */
public class ServerConnectAction extends MenuAction {
    private final Logger log = LoggerFactory.getLogger(ServerConnectAction.class);
    private final String host;
    private int port;
    private final String serverId;

    public ServerConnectAction(Context context, final String host, int port, final String serverId) {
        super(context, R.drawable.ic_power_settings_new_black_24dp, R.string.server_connect_action, host);
        this.host = host;
        this.port = port;
        this.serverId = serverId;
    }

    @Override
    protected void onClick(Context context) {
        log.info("connect to {} {}", host, port);
        Engine.instance().connectTo(serverId, host, port);
    }
}