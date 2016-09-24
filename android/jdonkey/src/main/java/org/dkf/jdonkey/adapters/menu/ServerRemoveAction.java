package org.dkf.jdonkey.adapters.menu;

import android.content.Context;
import org.dkf.jdonkey.R;
import org.dkf.jdonkey.core.ConfigurationManager;
import org.dkf.jdonkey.core.Constants;
import org.dkf.jdonkey.util.ServerUtils;
import org.dkf.jdonkey.views.MenuAction;
import org.dkf.jed2k.protocol.server.ServerMet;
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
