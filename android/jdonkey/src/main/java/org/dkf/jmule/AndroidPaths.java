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

    private static final boolean USE_EXTERNAL_STORAGE_DIR_ON_OR_AFTER_ANDROID_10 = true;

    private final Context context;
    //private final Application app;

    public AndroidPaths(Context app) {
        this.context = app;
    }

   // public File data(Context ctx) {
        //return new File(storage(), DOWNLOADS_PATH);
    //    return storage(ctx);
    //}

    public File data() {
        //return new File(storage(), DOWNLOADS_PATH);
        return storage();
    }

    //public File metadata(Context ctx) {
    //    return new File(storage(ctx), METADATA_PATH);
    //}

    //public File temp() {
    //    return new File(context.getExternalFilesDir(null), TEMP_PATH);
    //}

    private static final Logger LOG = LoggerFactory.getLogger(AndroidPaths.class);
/*
    private static File storage(Context ctx) {
        if (SystemUtils.hasAndroid10OrNewer()) {
            File externalDir = ctx.getExternalFilesDir(null);
            return new File(USE_EXTERNAL_STORAGE_DIR_ON_OR_AFTER_ANDROID_10 ?
                    externalDir : ctx.getFilesDir(),
                    STORAGE_PATH);
        }
*/
        /* For Older versions of Android where we used to have access to write to external storage
         *  <externalStoragePath>/FrostWire/
         */
/*        String path = ConfigurationManager.instance().getString(Constants.PREF_KEY_STORAGE_PATH, Environment.getExternalStorageDirectory().getAbsolutePath());
        if (path.toLowerCase().endsWith("/" + STORAGE_PATH.toLowerCase())) {
            return new File(path);
        } else {
            return new File(path, STORAGE_PATH);
        }
*/
        //String path = ConfigurationManager.instance().getString(Constants.PREF_KEY_STORAGE_PATH);
        /*if (path.toLowerCase().endsWith("/" + STORAGE_PATH.toLowerCase())) {
            return new File(path);
        } else {
            return new File(path, STORAGE_PATH);
        }
        */
        //return new File(path);
    //}

    private File storage() {
        if (SystemUtils.hasAndroid10OrNewer()) {
            File externalDir = context.getExternalFilesDir(null);
            LOG.info("storage: external path {}", externalDir);
            return USE_EXTERNAL_STORAGE_DIR_ON_OR_AFTER_ANDROID_10 ? externalDir : context.getFilesDir();

        }

        /* For Older versions of Android where we used to have access to write to external storage
         *  <externalStoragePath>/FrostWire/
         */
        String path = ConfigurationManager.instance().getStoragePath();
                //ConfigurationManager.instance().getString(Constants.PREF_KEY_STORAGE_PATH, Environment.getExternalStorageDirectory().getAbsolutePath());
        LOG.info("storage: internal path {}", path);
        return new File(ConfigurationManager.instance().getStoragePath());
        /*
        if (path.toLowerCase().endsWith("/" + STORAGE_PATH.toLowerCase())) {
            return new File(path);
        } else {
            return new File(path);
        }*/

        //String path = ConfigurationManager.instance().getString(Constants.PREF_KEY_STORAGE_PATH);
        /*if (path.toLowerCase().endsWith("/" + STORAGE_PATH.toLowerCase())) {
            return new File(path);
        } else {
            return new File(path, STORAGE_PATH);
        }
        */
        //return new File(path);
    }
}
