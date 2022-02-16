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
    private final Context context;

    private static final Map<Byte, String> fileTypeFolders = new HashMap<>();
    private static final Object fileTypeFoldersLock = new Object();

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
        //if (BTEngine.ctx.dataDir == null) {
        //    throw new RuntimeException("AndroidPaths.getRelativeFolderPath() BTEngine.ctx.dataDir is null, check your logic");
        //}

        byte fileType = AndroidPaths.getFileType(f.getAbsolutePath(), true);

        // "Music","Movies","Pictures","Download"
        String fileTypeSubfolder = AndroidPaths.getFileTypeExternalRelativeFolderName(fileType);

        // "Music/FrostWire"
        String mediaStoreFolderPrefix = fileTypeSubfolder + "/FrostWire";
        mediaStoreFolderPrefix = mediaStoreFolderPrefix.replace("//","/");

        String fullOriginalFilePath = f.getAbsolutePath();

        // BTEngine.ctx.dataDir -> /storage/emulated/0/Android/data/com.frostwire.android/files/FrostWire/TorrentData
        // Let's remove this from the fullOriginalFilePath and we should now have only either the file name by itself
        // or the torrent folders and sub-folders containing it
        String removedDataPathFromFilePath = fullOriginalFilePath.replace(Platforms.data().getAbsolutePath() + "/", "");

        // Single file download, not contained by folders or sub-folders
        if (removedDataPathFromFilePath.equals(f.getName())) {
            return mediaStoreFolderPrefix;
        }

        String fileFoldersWithoutDataPath = removedDataPathFromFilePath.replace(f.getName(), "");
        return (mediaStoreFolderPrefix + "/" + fileFoldersWithoutDataPath).replace("//","/");
    }
}
