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

import org.apache.commons.io.FilenameUtils;
import org.dkf.jmule.fragments.TransfersFragment;
import org.dkf.jmule.util.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author gubatron
 * @author aldenml
 * JED2K stores all files to one place without sub dirs
 */
public final class AndroidPaths {
    private static final boolean USE_EXTERNAL_STORAGE_DIR_ON_OR_AFTER_ANDROID_10 = true;
    private final Application app;

    private static final Map<Byte, String> fileTypeFolders = new HashMap<>();
    private static final Object fileTypeFoldersLock = new Object();

    public AndroidPaths(Application app) {
        this.app = app;
    }

    public File data() {
        if (SystemUtils.hasAndroid10OrNewer()) {
            if (SystemUtils.hasAndroid10()) {
                return app.getExternalFilesDir(null);
            }

            // On Android 11 and up, they finally let us use File objects in the public download directory as long as we have permission from the user
            return android11AndUpStorage();
        }

        /* For Older versions of Android where we used to have access to write to external storage
         *  <externalStoragePath>/Download/FrostWire/
         */
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
    }

    public static byte getFileType(String filePath, boolean returnTorrentsAsDocument) {
        byte result = Constants.FILE_TYPE_UNKNOWN;

        MediaType mt = MediaType.getMediaTypeForExtension(FilenameUtils.getExtension(filePath));

        if (mt != null) {
            result = (byte) mt.getId();
        }

        if (returnTorrentsAsDocument && result == Constants.FILE_TYPE_TORRENTS) {
            result = Constants.FILE_TYPE_DOCUMENTS;
        }

        return result;
    }

    public static File android11AndUpStorage() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }


    /**
     * FILE_TYPE_AUDIO -> "Music"
     * FILE_TYPE_VIDEOS -> "Movies"
     * ...
     * Based on Android's Environment.DIRECTORY_XXX constants
     * We'll use these for MediaStore relative path prefixes concatenated to "/FrostWire" so the user
     * can easily find what's been downloaded with FrostWire in external folders.
     */
    private static String getFileTypeExternalRelativeFolderName(byte fileType) {
        synchronized (fileTypeFoldersLock) {
            // thread safe lazy load check
            if (fileTypeFolders.size() == 0) {
                fileTypeFolders.put(Constants.FILE_TYPE_AUDIO, Environment.DIRECTORY_MUSIC);
                fileTypeFolders.put(Constants.FILE_TYPE_VIDEOS, Environment.DIRECTORY_MOVIES);
                fileTypeFolders.put(Constants.FILE_TYPE_RINGTONES, Environment.DIRECTORY_RINGTONES);
                fileTypeFolders.put(Constants.FILE_TYPE_PICTURES, Environment.DIRECTORY_PICTURES);
                fileTypeFolders.put(Constants.FILE_TYPE_TORRENTS, Environment.DIRECTORY_DOWNLOADS);
                fileTypeFolders.put(Constants.FILE_TYPE_DOCUMENTS, Environment.DIRECTORY_DOCUMENTS);
            }
        }
        return fileTypeFolders.get(fileType);
    }

    public static String getRelativeFolderPath(File f) {
        byte fileType = AndroidPaths.getFileType(f.getAbsolutePath(), true);
        // "Music","Movies","Pictures","Download"
        String fileTypeSubfolder = AndroidPaths.getFileTypeExternalRelativeFolderName(fileType);
        String mediaStoreFolderPrefix = fileTypeSubfolder + "/JED2K";
        mediaStoreFolderPrefix = mediaStoreFolderPrefix.replace("//","/");
        return mediaStoreFolderPrefix;
    }

    public static String getRelativeFolderPathFromFileInDownloads(File f) {
        // "Download/FrostWire"
        String commonRelativePrefix = Environment.DIRECTORY_DOWNLOADS + "/JED2K";
        commonRelativePrefix = commonRelativePrefix.replace("//", "/").replaceAll("/\\z", "");
        return commonRelativePrefix;
        /*
        String fullOriginalFilePath = f.getAbsolutePath();

        // Let's remove this from the fullOriginalFilePath and we should now have only either the file name by itself
        // or the torrent folders and sub-folders containing it
        String removedDataPathFromFilePath = fullOriginalFilePath;
        if (SystemUtils.hasAndroid10()) {
            // in case it's an internal file (android 10)
            removedDataPathFromFilePath = fullOriginalFilePath.
                    replace(BTEngine.ctx.dataDir.getAbsolutePath() + "/", "");
        }

        // this is the most likely prefix (android 11+)
        removedDataPathFromFilePath = removedDataPathFromFilePath.
                replace("/storage/emulated/0/", "");


        // Single file download, not contained by folders or sub-folders
        if (removedDataPathFromFilePath.equals(f.getName())) {
            return commonRelativePrefix;
        }

        String result = removedDataPathFromFilePath.replace(f.getName(), "").
                replaceAll("/\\z", ""); // remove trailing slash
        return result;
         */
    }
}
