package org.dkf.jmule.adapters.menu;

import android.content.Context;
import org.dkf.jmule.ConfigurationManager;
import org.dkf.jmule.Constants;
import org.dkf.jed2k.protocol.server.ServerMet;
import org.dkf.jmule.Engine;
import org.dkf.jmule.R;
import org.dkf.jmule.util.ServerUtils;
import org.dkf.jmule.views.MenuAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * Created by ap197_000 on 13.09.2016.
 */
public class ServerRemoveAction extends MenuAction {
    private final Logger log = LoggerFactory.getLogger(ServerRemoveAction.class);
    private final String serverId;

    public ServerRemoveAction(Context context, final String host, final String serverId) {
        super(context, R.drawable.ic_delete_forever_black_24dp, R.string.server_remove_action, host);
        this.serverId = serverId;
    }

    @Override
    protected void onClick(Context context) {
        ServerMet sm = new ServerMet();
        ConfigurationManager.instance().getSerializable(Constants.PREF_KEY_SERVERS_LIST, sm);
        if (sm != null) {
            Engine.instance().disconnectFrom(); // try to disconnect in any case
            Iterator<ServerMet.ServerMetEntry> iter = sm.getServers().iterator();
            while(iter.hasNext()) {
                ServerMet.ServerMetEntry entry = iter.next();
                if (ServerUtils.getIdentifier(entry).compareTo(serverId) == 0) {
                    log.info("remove key {}", ServerUtils.getIdentifier(entry));
                    iter.remove();
                    ConfigurationManager.instance().setSerializable(Constants.PREF_KEY_SERVERS_LIST, sm);
                    break;
                }
            }
        }
        else {
            log.warn("Server list is empty in configurations");
        }

    }
}
