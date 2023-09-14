/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml),
 *            Marcelina Knitter (@marcelinkaaa)
 * Copyright (c) 2011-2022, FrostWire(R). All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dkf.jmule;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.system.Os;

import androidx.annotation.RequiresApi;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.dkf.jed2k.util.StringUtils;
import org.dkf.jmule.core.FWFileDescriptor;
import org.dkf.jmule.core.providers.TableFetcher;
import org.dkf.jmule.core.providers.TableFetchers;
import org.dkf.jmule.util.Ref;
import org.dkf.jmule.util.SystemUtils;
import org.dkf.jmule.util.UIUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

import okio.BufferedSink;
import okio.Okio;

/**
 * The Librarian is in charge of:
 * -> Keeping track of what files we're sharing or not.
 * -> Indexing the files we're sharing.
 * -> Searching for files we're sharing.
 *
 * @author gubatron
 * @author aldenml
 */
public final class Librarian {
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(Librarian.class);
    private static final Object instanceCreationLock = new Object();
    private static Librarian instance;
    private Handler handler;

    public static Librarian instance() {
        if (instance != null) { // quick check to avoid lock
            return instance;
        }

        synchronized (instanceCreationLock) {
            if (instance == null) {
                instance = new Librarian();
            }
            return instance;
        }
    }

    private Librarian() {
        initHandler();
    }

    public void shutdownHandler() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    // Called by MyFileFragment.loadInBackground() -> createLoaderFiles() -> CreateLoaderFilesAsyncTaskLoader.loadInBackground()
    //
    public List<FWFileDescriptor> getFilesInAndroidMediaStore(final Context context, byte fileType, int offset, int pageSize) {
        return getFilesInAndroidMediaStore(context, offset, pageSize, TableFetchers.getFetcher(fileType));
    }

    public List<FWFileDescriptor> getFilesInAndroidMediaStore(final Context context, byte fileType, String where, String[] whereArgs) {
        return getFilesInAndroidMediaStore(context, 0, Integer.MAX_VALUE, TableFetchers.getFetcher(fileType), where, whereArgs);
    }

    /**
     * @param fileType the file type
     * @return the number of files registered in the providers
     */
    public int getNumFiles(Context context, byte fileType) {
        TableFetcher fetcher = TableFetchers.getFetcher(fileType);
        if (fetcher == TableFetchers.UNKNOWN_TABLE_FETCHER) {
            return 0;
        }

        Cursor c = null;

        int numFiles = 0;

        try {
            ContentResolver cr = context.getContentResolver();
            Uri externalContentUri = fetcher.getExternalContentUri();
            List<Uri> contentUris = new ArrayList<>();
            if (externalContentUri != null) {
                contentUris.add(externalContentUri);
            }
            for (Uri contentUri : contentUris) {
                c = cr.query(contentUri, new String[]{"count(" + BaseColumns._ID + ")"},
                        fetcher.where(), fetcher.whereArgs(), null);
                numFiles += c != null && c.moveToFirst() ? c.getInt(0) : 0;
            }
        } catch (Throwable e) {
            LOG.error("Failed to get num of files", e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return numFiles;
    }

    public FWFileDescriptor getFileDescriptor(final Context context, byte fileType, int fileId) {
        List<FWFileDescriptor> fds = getFilesInAndroidMediaStore(context, 0, 1, TableFetchers.getFetcher(fileType), BaseColumns._ID + "=?", new String[]{String.valueOf(fileId)});
        if (fds.size() > 0) {
            return fds.get(0);
        } else {
            return null;
        }
    }

    public String renameFile(final Context context, FWFileDescriptor fd, String newFileName) {
        try {
            String filePath = fd.filePath;
            File oldFile = new File(filePath);
            String ext = FilenameUtils.getExtension(filePath);
            File newFile = new File(oldFile.getParentFile(), newFileName + '.' + ext);
            ContentResolver cr = context.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaColumns.DATA, newFile.getAbsolutePath());
            values.put(MediaColumns.DISPLAY_NAME, FilenameUtils.getBaseName(newFileName));
            values.put(MediaColumns.TITLE, FilenameUtils.getBaseName(newFileName));
            TableFetcher fetcher = TableFetchers.getFetcher(fd.fileType);

            if (fetcher != TableFetchers.UNKNOWN_TABLE_FETCHER &&
                    fetcher.getExternalContentUri() != null) {
                try {
                    cr.update(fetcher.getExternalContentUri(), values, BaseColumns._ID + "=?", new String[]{String.valueOf(fd.id)});
                } catch (Throwable t) {
                    LOG.error(t.getMessage(), t);
                }
            }

            //noinspection ResultOfMethodCallIgnored
            oldFile.renameTo(newFile);
            return newFile.getAbsolutePath();
        } catch (Throwable e) {
            LOG.error("Failed to rename file: " + fd, e);
        }
        return null;
    }

    /**
     * Deletes files.
     * If the fileType is audio it'll use MusicUtils.deleteTracks and
     * tell apollo to clean everything there, playslists, recents, etc.
     */
    public void deleteFiles(final Context context, byte fileType, Collection<FWFileDescriptor> fds) {
        List<Integer> ids = new ArrayList<>(fds.size());
        final int audioMediaType = MediaType.getAudioMediaType().getId();
        if (fileType == audioMediaType) {
            ArrayList<Long> trackIdsToDelete = new ArrayList<>();
            for (FWFileDescriptor fd : fds) {
                // just in case, as we had similar checks in other code
                if (fd.fileType == audioMediaType) {
                    trackIdsToDelete.add((long) fd.id);
                    ids.add(fd.id);
                }
            }
            // wish I could do just trackIdsToDelete.toArray(new long[0]) ...
            long[] songsArray = new long[trackIdsToDelete.size()];
            int i = 0;
            for (Long l : trackIdsToDelete) {
                songsArray[i++] = l;
            }

            /*
            try {
                MusicUtils.deleteTracks(context, songsArray, false);
            } catch (Throwable t) {
                t.printStackTrace();
            }*/
        } else {
            for (FWFileDescriptor fd : fds) {
                ids.add(fd.id);
            }
        }

        try {
            if (context != null) {
                ContentResolver cr = context.getContentResolver();
                TableFetcher fetcher = TableFetchers.getFetcher(fileType);

                try {
                    if (fetcher != TableFetchers.UNKNOWN_TABLE_FETCHER && fetcher.getExternalContentUri() != null) {
                        cr.delete(fetcher.getExternalContentUri(), MediaColumns._ID + " IN " + buildSet(ids), null);
                    }
                } catch (Throwable t) {
                    LOG.error(t.getMessage(), t);
                }
            } else {
                LOG.error("Failed to delete files from media store, no context available");
            }
        } catch (Throwable e) {
            LOG.error("Failed to delete files from media store", e);
        }

        FileSystem fs = Platforms.fileSystem();
        for (FWFileDescriptor fd : fds) {
            try {
                fs.delete(new File(fd.filePath));
            } catch (Throwable ignored) {
            }
        }

        UIUtils.broadcastAction(context,
                Constants.ACTION_FILE_ADDED_OR_REMOVED,
                new UIUtils.IntentByteExtra(Constants.EXTRA_REFRESH_FILE_TYPE, fileType));
    }


    // must not be called from main thread
    public void scanMulti(final Context context, List<File> files) {
        for (File file: files) {
            scan(context, file, Collections.emptySet());
            if (context != null) {
                UIUtils.broadcastAction(context, Constants.ACTION_FILE_ADDED_OR_REMOVED);
            }
        }
    }

    /**
     * @see org.dkf.jmule.transfers.Transfer finished() calls this when a torrent download ends
     * on both the torrents folder and the data folder.
     */
    public void scan(final Context context, File file) {
        if (Thread.currentThread() != handler.getLooper().getThread()) {
            SystemUtils.exceptionSafePost(handler, () -> scan(context, file, Collections.emptySet()));
            return;
        }
        scan(context, file, Collections.emptySet());
        if (context == null) {
            LOG.error("Librarian has no `context` object to scan() with.");
            return;
        }
        UIUtils.broadcastAction(context, Constants.ACTION_FILE_ADDED_OR_REMOVED);
    }

    public void syncMediaStore(final WeakReference<Context> contextRef) {
        if (!SystemUtils.hasAndroid10OrNewer() && !SystemUtils.isPrimaryExternalStorageMounted()) {
            return;
        }
        SystemUtils.exceptionSafePost(handler, () -> syncMediaStoreSupport(contextRef));
    }


    /**
     * This method assumes you did the logic to determine the target location in Downloads.
     * Meaning, "destInDownloads" doesn't exist yet, but this is where you'd like it to be saved at.
     *
     * @param src             The actual file wherever else it exists, usually in an internal/external app folder
     * @param destInDownloads The final desired location of the file in the public Downloads folder
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static boolean mediaStoreSaveToDownloads(final Context context, File src, File destInDownloads, boolean copyBytesToMediaStore) {
        LOG.info("Librarian::mediaStoreSaveToDownloads trying to save " + src.getAbsolutePath() + " into " + destInDownloads.getAbsolutePath());

        if (context == null) {
            LOG.info("Librarian::mediaStoreSaveToDownloads aborting. ApplicationContext reference is null, not ready yet.");
            return false;
        }

        String relativePath = AndroidPaths.getRelativeFolderPathFromFileInDownloads(destInDownloads);

        if (Librarian.mediaStoreFileExists(context, destInDownloads)) {
            LOG.info("Librarian::mediaStoreSaveToDownloads aborting. " + relativePath + "/" + destInDownloads.getName() + " already exists on the media store db");
            return false;
        }

        return mediaStoreInsert(context, src, relativePath, copyBytesToMediaStore);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static boolean mediaStoreFileExists(final Context context, File destInDownloads) {
        String relativePath = AndroidPaths.getRelativeFolderPathFromFileInDownloads(destInDownloads);
        String displayName = destInDownloads.getName();
        Uri downloadsExternalUri = MediaStore.Downloads.getContentUri("external");
        ContentResolver contentResolver = context.getContentResolver();
        String selection = MediaColumns.DISPLAY_NAME + " = ? AND " +
                MediaColumns.RELATIVE_PATH + " LIKE ?";
        String[] selectionArgs = new String[]{displayName, '%' + relativePath + '%'};
        Cursor query = contentResolver.query(downloadsExternalUri, null, selection, selectionArgs, null);
        if (query == null) {
            LOG.info("Librarian::mediaStoreFileExists -> null query for " + displayName);
            return false;
        }
        boolean fileFound = query.getCount() > 0;
        query.close();
        LOG.info("Librarian::mediaStoreFileExists() -> " + fileFound);
        return fileFound;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private static boolean mediaStoreInsert(Context context, File srcFile, String relativeFolderPath, boolean copyBytesToMediaStore) {
        if (srcFile.isDirectory()) {
            return false;
        }
        // Add to MediaStore
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaColumns.IS_PENDING, copyBytesToMediaStore ? 1 : 0);

        if (!StringUtils.isNullOrEmpty(relativeFolderPath)) {
            LOG.info("Librarian::mediaStoreInsert using relative path " + relativeFolderPath);
            values.put(MediaColumns.RELATIVE_PATH, relativeFolderPath);
        } else {
            LOG.info("WARNING, relative relativeFolderPath is null for " + srcFile.getAbsolutePath());
        }

        values.put(MediaColumns.DISPLAY_NAME, srcFile.getName());
        values.put(MediaColumns.MIME_TYPE, MimeDetector.getMimeType(FilenameUtils.getExtension(srcFile.getName())));
        values.put(MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(MediaColumns.DATE_MODIFIED, System.currentTimeMillis() / 1000);
        values.put(MediaColumns.SIZE, srcFile.length());

        byte fileType = AndroidPaths.getFileType(srcFile.getAbsolutePath(), true);
        if (fileType == Constants.FILE_TYPE_AUDIO || fileType == Constants.FILE_TYPE_VIDEOS) {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            boolean illegalArgumentCaught = false;
            try {
                mmr.setDataSource(srcFile.getAbsolutePath());
            } catch (Throwable ignored) {
                // at first we tried catching illegal argument exception
                // then we started seeing Runtime Exception errors being thrown here.
                illegalArgumentCaught = true;
            }
            String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String artistName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String albumArtistName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST);
            String albumName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            String durationString = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (title != null) {
                LOG.info("mediaStoreInsert title (MediaDataRetriever): " + title);
                values.put(MediaColumns.TITLE, title);
                values.put(MediaColumns.DISPLAY_NAME, srcFile.getName());

                if (SystemUtils.hasAndroid11OrNewer() && fileType == Constants.FILE_TYPE_AUDIO) {
                    values.put(MediaColumns.ARTIST, artistName);
                    values.put(MediaColumns.ALBUM_ARTIST, albumArtistName);
                    values.put(MediaColumns.ALBUM, albumName);
                    if (!StringUtils.isNullOrEmpty(durationString)) {
                        values.put(MediaColumns.DURATION, Long.parseLong(durationString));
                    }
                }
            } else if (illegalArgumentCaught && title == null) {
                // Something went wrong with mmr.setDataSource()
                // Happens in Android 10
                String fileNameWithoutExtension = srcFile.getName().replace(
                        FilenameUtils.getExtension(srcFile.getName()), "");
                values.put(MediaColumns.TITLE, fileNameWithoutExtension);
                values.put(MediaColumns.DISPLAY_NAME, fileNameWithoutExtension);
            }
        } else {
            values.put(MediaColumns.TITLE, srcFile.getName());
        }
        Uri downloadsExternalUri = MediaStore.Downloads.getContentUri("external");
        try {
            Uri insertedUri = resolver.insert(downloadsExternalUri, values);
            if (insertedUri == null) {
                LOG.error("mediaStoreInsert -> could not perform media store insertion");
                return false;
            }
            LOG.info("mediaStoreInsert -> insertedUri = " + insertedUri);
            if (copyBytesToMediaStore) {
                return copyFileBytesToMediaStore(resolver, srcFile, values, insertedUri);
            }
        } catch (Throwable t) {
            return false;
        }
        return true;
    }

    /*
    public EphemeralPlaylist createEphemeralPlaylist(final Context context, FWFileDescriptor fd) {

        if (!fd.deletable) {
            List<FWFileDescriptor> fds = getFilesInAndroidMediaStore(context, Constants.FILE_TYPE_AUDIO, FilenameUtils.getPath(fd.filePath), false);

            if (fds.size() == 0) { // just in case
                LOG.error("Logic error creating ephemeral playlist");
                fds.add(fd);
            }

            EphemeralPlaylist playlist = new EphemeralPlaylist(fds);
            playlist.setNextItem(new PlaylistItem(fd));

            return playlist;
        } else {
            List<FWFileDescriptor> fsListOfOne = new ArrayList<>();
            fsListOfOne.add(fd);
            EphemeralPlaylist playlist = new EphemeralPlaylist(fsListOfOne);
            playlist.setNextItem(new PlaylistItem(fd));

            return playlist;
        }
    }
*/
    private void syncMediaStoreSupport(final WeakReference<Context> contextRef) {
        if (!Ref.alive(contextRef)) {
            return;
        }
        Context context = contextRef.get();
        Set<File> ignorableFiles = Collections.emptySet();
        syncMediaStore(context, Constants.FILE_TYPE_AUDIO, ignorableFiles);
        syncMediaStore(context, Constants.FILE_TYPE_PICTURES, ignorableFiles);
        syncMediaStore(context, Constants.FILE_TYPE_VIDEOS, ignorableFiles);
        syncMediaStore(context, Constants.FILE_TYPE_RINGTONES, ignorableFiles);
        syncMediaStore(context, Constants.FILE_TYPE_DOCUMENTS, ignorableFiles);
        Platforms.fileSystem().scan(Platforms.data());
        //Platforms.fileSystem().scan(BTEngine.ctx.dataDir);
    }

    private void syncMediaStore(final Context context, byte fileType, Set<File> ignorableFiles) {
        TableFetcher fetcher = TableFetchers.getFetcher(fileType);

        if (fetcher == TableFetchers.UNKNOWN_TABLE_FETCHER) {
            return;
        }

        try {
            ContentResolver cr = context.getContentResolver();
            deleteIgnorableFilesFromVolume(cr, fetcher.getExternalContentUri(), ignorableFiles);
        } catch (Throwable e) {
            LOG.error("General failure during sync of MediaStore", e);
        }
    }

    private void deleteIgnorableFilesFromVolume(ContentResolver cr, Uri volumeUri, Set<File> ignorableFiles) {
        if (volumeUri == null) {
            return;
        }
        String where = MediaColumns.DATA + " LIKE ?";
        String[] whereArgs = new String[]{Platforms.data() + "%"};

        Cursor c = cr.query(volumeUri, new String[]{MediaColumns._ID, MediaColumns.DATA}, where, whereArgs, null);
        if (c == null) {
            return;
        }

        int idCol = c.getColumnIndex(MediaColumns._ID);
        int pathCol = c.getColumnIndex(MediaColumns.DATA);

        List<Integer> ids = new ArrayList<>(0);

        while (c.moveToNext()) {
            int id = Integer.parseInt(c.getString(idCol));
            String path = c.getString(pathCol);

            if (ignorableFiles.contains(new File(path))) {
                ids.add(id);
            }
        }

        try {
            if (ids.size() > 0) {
                cr.delete(volumeUri, MediaColumns._ID + " IN " + buildSet(ids), null);
            }
        } catch (Throwable e) {
            LOG.error("General failure during sync of MediaStore", e);
        } finally {
            c.close();
        }
    }

    private List<FWFileDescriptor> getFilesInAndroidMediaStore(final Context context, int offset, int pageSize, TableFetcher fetcher) {
        return getFilesInAndroidMediaStore(context, offset, pageSize, fetcher, null, null);
    }

    /**
     * Returns a list of FWFileDescriptors.
     *
     * @param offset   - from where (starting at 0)
     * @param pageSize - how many results
     * @param fetcher  - An implementation of TableFetcher
     * @return List<FileDescriptor>
     */
    public List<FWFileDescriptor> getFilesInAndroidMediaStore(
            final Context context,
            final int offset,
            final int pageSize,
            final TableFetcher fetcher,
            String where,
            String[] whereArgs) {
        final List<FWFileDescriptor> result = new ArrayList<>(0);

        if (context == null || fetcher == null || fetcher == TableFetchers.UNKNOWN_TABLE_FETCHER) {
            return result;
        }

        try {
            ContentResolver cr = context.getContentResolver();
            String[] columns = fetcher.getColumns();
            String sort = fetcher.getSortByExpression();

            if (where == null) {
                where = fetcher.where();
                whereArgs = fetcher.whereArgs();
            }

            try {
                getFilesInVolume(cr, fetcher.getExternalContentUri(), offset, pageSize, columns, sort,
                        where, whereArgs, fetcher, result);
            } catch (Throwable t) {
                LOG.error("getFiles::getFilesInVolume failed with fetcher.getExternalContentUri() = " + fetcher.getExternalContentUri(), t);
            }
        } catch (Throwable e) {
            LOG.error("General failure getting files", e);
        }
        return result;
    }

    public void getFilesInVolume(final ContentResolver cr,
                                 final Uri volumeUri,
                                 final int offset,
                                 final int pageSize,
                                 final String[] columns,
                                 final String sort,
                                 final String where,
                                 final String[] whereArgs,
                                 final TableFetcher fetcher,
                                 final List<FWFileDescriptor> result) {
        if (volumeUri == null) {
            return;
        }
        try {
            Cursor c = cr.query(volumeUri, columns, where, whereArgs, sort);
            if (c == null || !c.moveToPosition(offset)) {
                return;
            }
            fetcher.prepareColumnIds(c);
            int count = 1;
            do {
                FWFileDescriptor fd = fetcher.fetchFWFileDescriptor(c);
                result.add(fd);
            } while (c.moveToNext() && count++ < pageSize);
            IOUtils.closeQuietly(c);
        } catch (Throwable e) {
            LOG.error(e.getMessage() + " volumeUri=" + volumeUri, e);
        }
    }

    public List<FWFileDescriptor> getFilesInAndroidMediaStore(final Context context, String filepath, boolean exactPathMatch) {
        return getFilesInAndroidMediaStore(context, AndroidPaths.getFileType(filepath, true), filepath, exactPathMatch);
    }

    /**
     * @param exactPathMatch - set it to false and pass an incomplete filepath prefix to get files in a folder for example.
     */
    public List<FWFileDescriptor> getFilesInAndroidMediaStore(final Context context, byte fileType, String filepath, boolean exactPathMatch) {
        String where = MediaColumns.DATA + " LIKE ?";
        String[] whereArgs = new String[]{(exactPathMatch) ? filepath : "%" + filepath + "%"};
        return getFilesInAndroidMediaStore(context, fileType, where, whereArgs);
    }

    public Thread getHandlerThread() {
        return handler.getLooper().getThread();
    }

    public static boolean createSymLink(String originalFilePath, String symLinkFilePath) {
        try {
            Os.symlink(originalFilePath, symLinkFilePath);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void scan(final Context context, File file, Set<File> ignorableFiles) {
        //if we just have a single file, do it the old way
        if (file.isFile()) {
            if (ignorableFiles.contains(file)) {
                return;
            }
            if (SystemUtils.hasAndroid10OrNewer()) {
                // Can't use Media Scanner after Android 10 Scoped storage changes.
                // MediaScanner is supposedly invoked internally when we perform MediaStore inserts/updates
                // it will set the DATA field for us, so don't try to write it manually, doesn't keep
                // whatever path you put in there
                mediaStoreInsert(context, file);
            } else {
                new UniversalScanner(context).scan(file.getAbsolutePath());
            }
        } else if (file.isDirectory() && file.canRead()) {
            Collection<File> flattenedFiles = getAllFolderFiles(file, null);

            if (ignorableFiles != null && !ignorableFiles.isEmpty()) {
                flattenedFiles.removeAll(ignorableFiles);
            }

            if (!flattenedFiles.isEmpty()) {
                if (SystemUtils.hasAndroid10OrNewer()) {
                    flattenedFiles.forEach(f -> mediaStoreInsert(context, f));
                } else {
                    new UniversalScanner(context).scan(flattenedFiles);
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private static boolean copyFileBytesToMediaStore(ContentResolver contentResolver,
                                                     File srcFile,
                                                     ContentValues values,
                                                     Uri insertedUri) {
        try {
            OutputStream outputStream = contentResolver.openOutputStream(insertedUri);
            if (outputStream == null) {
                LOG.error("Librarian::copyFileBytesToMediaStore failed, could not get an output stream from insertedUri=" + insertedUri);
                return false;
            }
            BufferedSink sink = Okio.buffer(Okio.sink(outputStream));
            sink.writeAll(Okio.source(srcFile));
            sink.flush();
            sink.close();
        } catch (Throwable t) {
            LOG.error("Librarian::copyFileBytesToMediaStore error: " + t.getMessage(), t);
            return false;
        }
        values.clear();
        values.put(MediaColumns.IS_PENDING, 0);
        contentResolver.update(insertedUri, values, null, null);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void mediaStoreInsert(Context context, File srcFile) {
        if (srcFile.isDirectory()) {
            return;
        }

        Uri audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        // Add to MediaStore
        ContentResolver resolver = context.getContentResolver();
        byte fileType = AndroidPaths.getFileType(srcFile.getAbsolutePath(), true);
        TableFetcher fetcher = TableFetchers.getFetcher(fileType);

        if (fetcher == TableFetchers.UNKNOWN_TABLE_FETCHER) {
            LOG.info("mediaStoreInsert -> fetcher unknown for " + srcFile.getAbsolutePath() + ", skipping");
            return;
        }

        Uri mediaStoreCollectionUri = Objects.requireNonNull(fetcher).getExternalContentUri();
        String relativeFolderPath = AndroidPaths.getRelativeFolderPath(srcFile);

        //LOG.info("mediaStoreInsert -> rel path {}", relativeFolderPath);
        if (alreadyInMediaStore(context, fetcher, srcFile.getName(), relativeFolderPath)) {
            LOG.info("mediaStoreInsert: alreadyInMediaStore skipping " + srcFile.getAbsolutePath());
            return;
        }

        LOG.info("mediaStoreInsert -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI = " + audioUri);
        LOG.info("mediaStoreInsert -> mediaStoreCollectionUri = " + mediaStoreCollectionUri);
        LOG.info("mediaStoreInsert -> relativeFolderPath: " + relativeFolderPath);

        ContentValues values = new ContentValues();
        values.put(MediaColumns.IS_PENDING, 1);

        if (!StringUtils.isNullOrEmpty(relativeFolderPath)) {
            values.put(MediaColumns.RELATIVE_PATH, relativeFolderPath);
        } else {
            LOG.info("WARNING, relative relativeFolderPath is null for " + srcFile.getAbsolutePath());
        }

        values.put(MediaColumns.DISPLAY_NAME, srcFile.getName());
        values.put(MediaColumns.TITLE, srcFile.getName());
        values.put(MediaColumns.MIME_TYPE, MimeDetector.getMimeType(FilenameUtils.getExtension(srcFile.getName())));
        values.put(MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(MediaColumns.DATE_MODIFIED, System.currentTimeMillis() / 1000);
        values.put(MediaColumns.SIZE, srcFile.length());

        if (fileType == Constants.FILE_TYPE_AUDIO || fileType == Constants.FILE_TYPE_VIDEOS) {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(srcFile.getAbsolutePath());
            String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            LOG.info("mediaStoreInsert title (MediaDataRetriever): " + title);
            if (title != null) {
                values.put(MediaColumns.TITLE, title);
                values.put(MediaColumns.DISPLAY_NAME, srcFile.getName());
            }
        } else {
            values.put(MediaColumns.TITLE, srcFile.getName());
        }

        Uri insertedUri = resolver.insert(mediaStoreCollectionUri, values);
        if (insertedUri == null) {
            LOG.error("mediaStoreInsert -> could not perform media store insertion");
            return;
        }

        copyFileBytesToMediaStore(resolver, srcFile, values, insertedUri);
    }

    private boolean alreadyInMediaStore(Context context, TableFetcher fetcher, final String displayName, final String relativeFolderPath) {
        // relativePath looks like:
        // Documents/FrostWire/storage/emulated/0/Android/data/com.frostwire.android/files/FrostWire/Torrents/
        // but the database may have relative paths stored like:
        // Documents/FrostWire/(invalid)/storage/emulated/0/Android/data/com.frostwire.android/files/FrostWire/Torrents/
        // let's fix our relativePath search to be only from "com.frostwire.android/..."
        String normalizedRelativePath = relativeFolderPath; // as is

        String normalizedDisplayName = displayName;
        String extension = FilenameUtils.getExtension(displayName);
        if (extension != null && !"".equals(extension)) {
            normalizedDisplayName = displayName.replace("." + extension, "");
        }

        // Depending on file type we use different search fields for the file title (see mediaStoreInsert)
        String[] projection_audio_video = new String[]{MediaColumns._ID, MediaColumns.DISPLAY_NAME, MediaColumns.RELATIVE_PATH};
        String[] projection_other = new String[]{MediaColumns._ID, MediaColumns.TITLE, MediaColumns.RELATIVE_PATH};

        String selection_audio_video = MediaColumns.DISPLAY_NAME + " LIKE ? AND " + MediaColumns.RELATIVE_PATH + " LIKE ?";
        String selection_other = MediaColumns.TITLE + " LIKE ? AND " + MediaColumns.RELATIVE_PATH + " LIKE ?";

        Uri volumeUri = fetcher.getExternalContentUri();
        byte fileType = AndroidPaths.getFileType(displayName, true);
        if (fileType == Constants.FILE_TYPE_UNKNOWN) {
            return false;
        }

        String[] projection = projection_other;
        String selection = selection_other;

        if (fileType == Constants.FILE_TYPE_AUDIO || fileType == Constants.FILE_TYPE_VIDEOS) {
            projection = projection_audio_video;
            selection = selection_audio_video;
        }

        try {
            ContentResolver cr = context.getContentResolver();
            Cursor cursor = null;
            try {
                cursor = cr.query(volumeUri, projection, selection, new String[]{"%" + normalizedDisplayName + "%", "%" + normalizedRelativePath + "%"}, null);
            } catch (Throwable t) {
                LOG.error("alreadyInMediaStore: " + t.getMessage(), t);
            }
            if (cursor == null) {
                return false;
            }

            int totalResults = cursor.getCount();
            if (totalResults == 0) {
                IOUtils.closeQuietly(cursor);
                return false;
            }

            cursor.moveToFirst();

            List<Long> fileIdsToDelete = new ArrayList<>();
            do {
                int displayNameColIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
                if (displayNameColIndex == -1) {
                    displayNameColIndex = cursor.getColumnIndex(MediaColumns.TITLE);
                }

                // Check if there are repeated entries for the file we're looking for/
                // If the display name without the extension is a prefix of the current row, we have a duplicate
                // <normalized display name> (N).<ext>
                String currentDisplayName = cursor.getString(displayNameColIndex);

                if (!currentDisplayName.equals(displayName) && currentDisplayName.startsWith(normalizedDisplayName)) {
                    int idColumnIndex = cursor.getColumnIndex(MediaColumns._ID);
                    long fileId = cursor.getLong(idColumnIndex);
                    fileIdsToDelete.add(fileId);
                }
            } while (cursor.moveToNext());
            boolean result = cursor.getCount() > 0;
            IOUtils.closeQuietly(cursor);
            // delete any found duplicates
            if (fileIdsToDelete.size() > 1) {
                fileIdsToDelete.forEach(fileId -> cr.delete(volumeUri, "_id = ?", new String[]{String.valueOf(fileId)}));
                fileIdsToDelete.clear();
            }
            return result;
        } catch (Throwable e) {
            LOG.error(e.getMessage() + " volumeUri=" + volumeUri, e);
        }
        return false;
    }

    private void initHandler() {
        final HandlerThread handlerThread = new HandlerThread("Librarian::handler",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    /**
     * Given a folder path it'll return all the files contained within it and it's subfolders
     * as a flat set of Files.
     * <p>
     * Non-recursive implementation, up to 20% faster in tests than recursive implementation. :)
     *
     * @param extensions If you only need certain files filtered by their extensions, use this string array (without the "."). or set to null if you want all files. e.g. ["txt","jpg"] if you only want text files and jpegs.
     * @return The set of files.
     * @author gubatron
     */
    private static Collection<File> getAllFolderFiles(File folder, String[] extensions) {
        Set<File> results = new HashSet<>();
        Stack<File> subFolders = new Stack<>();
        File currentFolder = folder;
        while (currentFolder != null && currentFolder.isDirectory() && currentFolder.canRead()) {
            File[] fs = null;
            try {
                fs = currentFolder.listFiles();
            } catch (SecurityException e) {
                LOG.error(e.getMessage(), e);
            }

            if (fs != null && fs.length > 0) {
                for (File f : fs) {
                    if (!f.isDirectory()) {
                        if (extensions == null || FilenameUtils.isExtension(f.getName(), extensions)) {
                            results.add(f);
                        }
                    } else {
                        subFolders.push(f);
                    }
                }
            }

            if (!subFolders.isEmpty()) {
                currentFolder = subFolders.pop();
            } else {
                currentFolder = null;
            }
        }
        return results;
    }

    private static String buildSet(List<?> list) {
        StringBuilder sb = new StringBuilder("(");
        int i = 0;
        for (Object id : list) {
            sb.append(id);
            if (i++ < (list.size() - 1)) {
                sb.append(",");
            }
        }
        sb.append(")");

        return sb.toString();
    }

    public Handler getHandler() {
        return handler;
    }
}
