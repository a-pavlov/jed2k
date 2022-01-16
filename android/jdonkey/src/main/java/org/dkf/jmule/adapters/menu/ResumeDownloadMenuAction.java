/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2016, FrostWire(R). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.dkf.jmule.adapters.menu;

import android.content.Context;
import org.dkf.jmule.NetworkManager;
import org.dkf.jmule.Engine;
import org.dkf.jmule.R;
import org.dkf.jmule.transfers.Transfer;
import org.dkf.jmule.util.UIUtils;
import org.dkf.jmule.views.MenuAction;

/**
 * @author gubatron
 * @author aldenml
 */
public final class ResumeDownloadMenuAction extends MenuAction {

    private final Transfer download;

    public ResumeDownloadMenuAction(Context context, Transfer download, int stringId) {
        super(context, R.drawable.ic_play_circle_outline_black_24dp, stringId);
        this.download = download;
    }

    @Override
    protected void onClick(Context context) {
        boolean isDisconnected = Engine.instance().isStopped();
        if (isDisconnected) {
            UIUtils.showLongMessage(context, R.string.cant_resume_torrent_transfers);
        } else {
            if (NetworkManager.instance().isDataUp()) {
                if (download.isPaused()) {
                    download.resume();
                }
            } else {
                UIUtils.showShortMessage(context, R.string.please_check_connection_status_before_resuming_download);
            }
        }
    }
}
