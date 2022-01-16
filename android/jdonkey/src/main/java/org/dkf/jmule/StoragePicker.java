/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml), Emil Suleymanov (sssemil)
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

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.v4.provider.DocumentFile;
import android.support.v4.util.Pair;
import org.dkf.jmule.AndroidFileHandler;
import org.dkf.jmule.LollipopFileSystem;
import org.dkf.jmule.Platforms;
import org.dkf.jmule.util.UIUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

/**
 * @author gubatron
 * @author aldenml
 */
public final class StoragePicker {

    private static final Logger LOG = LoggerFactory.getLogger(StoragePicker.class);

    public static final String ACTION_OPEN_DOCUMENT_TREE = "android.intent.action.OPEN_DOCUMENT_TREE";

    public static final int SELECT_FOLDER_REQUEST_CODE = 1267123;

    private static final String EXTRA_SHOW_ADVANCED = "android.content.extra.SHOW_ADVANCED";

    private StoragePicker() {
    }

    public static void show(Activity activity) {
        Intent intent = new Intent(ACTION_OPEN_DOCUMENT_TREE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.putExtra(EXTRA_SHOW_ADVANCED, true);
        activity.startActivityForResult(intent, SELECT_FOLDER_REQUEST_CODE);
    }

    public static String handle(Context context, int requestCode, int resultCode, Intent data) {
        String result = null;
        try {

            if (resultCode == Activity.RESULT_OK && requestCode == SELECT_FOLDER_REQUEST_CODE) {
                Uri treeUri = data.getData();

                ContentResolver cr = context.getContentResolver();

                Method takePersistableUriPermissionM = cr.getClass().getMethod("takePersistableUriPermission", Uri.class, int.class);
                final int takeFlags = data.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                takePersistableUriPermissionM.invoke(cr, treeUri, takeFlags);

                if (treeUri == null) {
                    UIUtils.showShortMessage(context, R.string.storage_picker_treeuri_null);
                    result = null;
                } else {
                    DocumentFile file = DocumentFile.fromTreeUri(context, treeUri);
                    if (!file.isDirectory()) {
                        UIUtils.showShortMessage(context, R.string.storage_picker_treeuri_not_directory);
                        result = null;
                    } else if (!file.canWrite()) {
                        UIUtils.showShortMessage(context, R.string.storage_picker_treeuri_cant_write);
                        result = null;
                    } else {
                        if (Platforms.get().saf()) {
                            LollipopFileSystem fs = (LollipopFileSystem) Platforms.fileSystem();
                            result = fs.getTreePath(treeUri);
                            LOG.info("result {}", result);

                            // TODO - remove below code - only for testing SD card writing
                            File testFile = new File(result, "test_file.txt");
                            LOG.info("test file {}", testFile);

                            try {
                                Pair<ParcelFileDescriptor, DocumentFile> fd = fs.openFD(testFile, "rw");
                                if (fd != null && fd.first != null && fd.second != null) {
                                    AndroidFileHandler ah = new AndroidFileHandler(testFile
                                            , fd.second
                                            , fd.first);
                                    ByteBuffer bb = ByteBuffer.allocate(48);
                                    bb.putInt(1).putInt(2).putInt(3).putInt(44).putInt(22);
                                    bb.flip();
                                    ah.getWriteChannel().write(bb);
                                    ah.close();
                                    boolean del = fs.delete(testFile);
                                    if (!del) {
                                        LOG.error("unable to delete file {}", testFile);
                                    }
                                }
                                else {
                                    LOG.error("unable to create file {}", testFile);
                                    result = null;
                                }
                            } catch (Exception e) {
                                LOG.error("unable to fill file {} error {}"
                                        , testFile, e);
                                result = null;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            UIUtils.showShortMessage(context, R.string.storage_picker_treeuri_error);
            LOG.error("Error handling folder selection {}", e);
            result = null;
        }

        return result;
    }
}
