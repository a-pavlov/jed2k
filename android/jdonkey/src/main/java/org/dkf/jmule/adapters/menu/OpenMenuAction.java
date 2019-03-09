/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2015, FrostWire(R). All rights reserved.
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
import org.dkf.jmule.R;
import org.dkf.jmule.util.UIUtils;
import org.dkf.jmule.views.MenuAction;

import java.io.File;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class OpenMenuAction extends MenuAction {
    private final File file;

    public OpenMenuAction(Context context, String title, File file) {
        super(context, R.drawable.ic_open_in_browser_black_24dp, R.string.open_menu_action, title);
        this.file = file;
    }

    public OpenMenuAction(Context context, File file) {
        super(context, R.drawable.ic_open_in_browser_black_24dp, R.string.open);
        this.file = file;
    }

    @Override
    protected void onClick(Context context) {
        if (file!=null) UIUtils.openFile(context, file);
    }
}
