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

package org.dkf.jmule;


import android.app.Application;
import android.content.Context;
import android.os.Environment;

import org.dkf.jmule.fragments.TransfersFragment;
import org.dkf.jmule.util.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author gubatron
 * @author aldenml
 * JED2K stores all files to one place without sub dirs
 */
public final class AndroidPaths {
    private static final boolean USE_EXTERNAL_STORAGE_DIR_ON_OR_AFTER_ANDROID_10 = true;
    private final Context context;

    public AndroidPaths(Context app) {
        this.context = app;
    }

    public File data() {
        if (SystemUtils.hasAndroid10OrNewer()) {
            File externalDir = context.getExternalFilesDir(null);
            return USE_EXTERNAL_STORAGE_DIR_ON_OR_AFTER_ANDROID_10 ? externalDir : context.getFilesDir();

        }

        /* For Older versions of Android where we used to have access to write to external storage
         *  <externalStoragePath>
         */
        String path = ConfigurationManager.instance().getStoragePath();
        return new File(ConfigurationManager.instance().getStoragePath());
    }
}
