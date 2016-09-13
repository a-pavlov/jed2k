package org.dkf.jdonkey.adapters.menu;

import android.content.Context;
import org.dkf.jdonkey.R;
import org.dkf.jdonkey.views.MenuAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ap197_000 on 13.09.2016.
 */
public class ServerRemoveAction extends MenuAction {
    private final Logger log = LoggerFactory.getLogger(ServerRemoveAction.class);
    private final String serverId;

    public ServerRemoveAction(Context context, final String host, final String serverId) {
        super(context, R.drawable.contextmenu_icon_remove_transfer, R.string.server_remove_action, host);
        this.serverId = serverId;
    }

    @Override
    protected void onClick(Context context) {
        log.info("remove server action");
    }
}
