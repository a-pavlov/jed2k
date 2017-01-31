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

package org.dkf.jed2k.android;


import android.content.Context;

import java.io.File;

/**
 * @author gubatron
 * @author aldenml
 */
public final class AndroidPaths {

    /**
     * base storage path
     */
    private static final String STORAGE_PATH = "Mule_on_Android";

    /**
     *  downloaded files path
     */
    private static final String DOWNLOADS_PATH = "";

    /**
     * metadata path for fast resume data saving
     */
    private static final String METADATA_PATH = ".metadata";

    private static final String TEMP_PATH = "temp";

    private final Context context;

    public AndroidPaths(Context app) {
        this.context = app;
    }

    public File data() {
        return new File(storage(), DOWNLOADS_PATH);
    }

    public File metadata() {
        return new File(storage(), METADATA_PATH);
    }

    public File temp() {
        return new File(context.getExternalFilesDir(null), TEMP_PATH);
    }

    private static File storage() {
        String path = ConfigurationManager.instance().getString(Constants.PREF_KEY_STORAGE_PATH);
        if (path.toLowerCase().endsWith("/" + STORAGE_PATH.toLowerCase())) {
            return new File(path);
        } else {
            return new File(path, STORAGE_PATH);
        }
    }
}
