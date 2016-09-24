/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2013, FrostWire(R). All rights reserved.
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

package org.dkf.jdonkey.adapters.menu;

import android.content.Context;
import org.dkf.jdonkey.R;
import org.dkf.jdonkey.transfers.Transfer;
import org.dkf.jdonkey.views.MenuAction;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public final class PauseDownloadMenuAction extends MenuAction {

    private final Transfer download;

    public PauseDownloadMenuAction(Context context, Transfer download) {
        super(context, R.drawable.ic_pause_circle_outline_black_24dp, R.string.pause_torrent_menu_action);
        this.download = download;
    }

    @Override
    protected void onClick(Context context) {
        if (!download.isPaused()) {
            download.pause();
        }
    }
}
