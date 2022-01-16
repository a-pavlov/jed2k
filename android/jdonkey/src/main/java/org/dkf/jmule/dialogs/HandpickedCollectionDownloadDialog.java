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

package org.dkf.jmule.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import org.apache.commons.io.FilenameUtils;
import org.dkf.jed2k.EMuleLink;
import org.dkf.jmule.MediaType;
import org.dkf.jed2k.util.Ref;
import org.dkf.jmule.Engine;
import org.dkf.jmule.R;
import org.dkf.jmule.util.UIUtils;
import org.slf4j.Logger;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * Created on 4/19/16.
 *
 * @author gubatron
 * @author aldenml
 *
 */
@SuppressWarnings("WeakerAccess") // We need the class to be public so that the dialog can be rotated (via Reflection)
public class HandpickedCollectionDownloadDialog extends AbstractConfirmListDialog<EMuleLink> {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(HandpickedCollectionDownloadDialog.class);
    private List<EMuleLink> links;

    public HandpickedCollectionDownloadDialog() { super();  }

    public static HandpickedCollectionDownloadDialog newInstance(
            Context ctx,
            final List<EMuleLink> links) {
        //
        // ideas:  - pre-selected file(s) to just check the one(s)
        //         - passing a file path
        //         - passing a byte[] to create the tinfo from.

        HandpickedCollectionDownloadDialog dlg = new HandpickedCollectionDownloadDialog();

        // this creates a bundle that gets passed to setArguments(). It's supposed to be ready
        // before the dialog is attached to the underlying activity, after we attach to it, then
        // we are able to use such Bundle to create our adapter.

        boolean[] allChecked = new boolean[links.size()];
        for (int i=0; i < allChecked.length; i++) {
            allChecked[i] = true;
        }

        dlg.onAttach((Activity) ctx);
        dlg.prepareArguments(R.drawable.download_icon,
                ctx.getString(R.string.emule_collection),
                ctx.getString(R.string.pick_the_files_you_want_to_download_from_this_torrent),
                "parameters",
                SelectionMode.MULTIPLE_SELECTION);
        final Bundle arguments = dlg.getArguments();
        //arguments.putByteArray(BUNDLE_KEY_TORRENT_INFO_DATA, tinfo.bencode());
        //arguments.putString(BUNDLE_KEY_MAGNET_URI, magnetUri);
        //arguments.putBooleanArray(BUNDLE_KEY_CHECKED_OFFSETS, allChecked);
        dlg.links = links;

        dlg.setOnYesListener(new OnStartDownloadsClickListener(ctx, dlg));
        return dlg;
    }


    @Override
    protected View.OnClickListener createOnYesListener(AbstractConfirmListDialog dlg) {
        return new OnStartDownloadsClickListener(getActivity(), dlg);
    }


    @Override
    public List<EMuleLink> deserializeData(String listDataInJSON) {
        return links;
    }

    @Override
    public ConfirmListDialogDefaultAdapter<EMuleLink> createAdapter(Context context, List<EMuleLink> listData, SelectionMode selectionMode, Bundle bundle) {
        Collections.sort(listData, new NameComparator());
        return new HandpickedTorrentFileEntriesDialogAdapter(context, listData, selectionMode);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState); //saves the torrentInfo in bytes.

    }

    @Override
    protected void initComponents(Dialog dlg, Bundle savedInstanceState) {
        super.initComponents(dlg, savedInstanceState);

    }

    private static class TorrentFileEntryList {
        final List<TorrentFileEntry> list = new ArrayList<>();
        public void add(TorrentFileEntry entry) {
            list.add(entry);
        }
    }

    static class TorrentFileEntry {
        private final int index;
        private final String name;
        private final String path;
        private final long size;

        TorrentFileEntry(int index, String name, String path, long size) {
            this.index = index;
            this.name = name;
            this.path = path;
            this.size = size;
        }

        public int getIndex() {
            return index;
        }

        public String getDisplayName() {
            return name;
        }

        public String getPath() {
            return path;
        }

        public long getSize() {
            return size;
        }
    }

    private class HandpickedTorrentFileEntriesDialogAdapter extends ConfirmListDialogDefaultAdapter<EMuleLink> {

        HandpickedTorrentFileEntriesDialogAdapter(Context context,
                                                  List<EMuleLink> list,
                                                  SelectionMode selectionMode) {
            super(context, list, selectionMode);
        }

        @Override
        public CharSequence getItemTitle(EMuleLink data) {
            return data.getStringValue();
        }

        @Override
        public long getItemSize(EMuleLink data) {
            return data.getNumberValue();
        }

        @Override
        public CharSequence getItemThumbnailUrl(EMuleLink data) {
            return null;
        }

        @Override
        public int getItemThumbnailResourceId(EMuleLink data) {
            return MediaType.getFileTypeIconId(FilenameUtils.getExtension(data.getStringValue()));
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            return super.getView(position, view, parent);
        }

        @Override
        public String getCheckedSum() {
            if (checked == null || checked.isEmpty()) {
                return null;
            }
            long totalBytes = 0;
            for (EMuleLink entry : (Set<EMuleLink>) checked) {
                totalBytes += entry.getNumberValue();
            }
            return UIUtils.getBytesInHuman(totalBytes);
        }
    }

    private static class OnStartDownloadsClickListener implements View.OnClickListener {
        private final WeakReference<Context> ctxRef;
        private WeakReference<AbstractConfirmListDialog> dlgRef;

        OnStartDownloadsClickListener(Context ctx, AbstractConfirmListDialog dlg) {
            ctxRef = new WeakReference<>(ctx);
            dlgRef = new WeakReference<>(dlg);
        }

        public void setDialog(AbstractConfirmListDialog dlg) {
            dlgRef = new WeakReference<>(dlg);
        }

        @Override
        public void onClick(View v) {
            if (Ref.alive(ctxRef) && Ref.alive(dlgRef)) {
                final AbstractConfirmListDialog dlg = dlgRef.get();

                final AbstractConfirmListDialog.SelectionMode selectionMode = dlg.getSelectionMode();
                List<EMuleLink> checked = (selectionMode == AbstractConfirmListDialog.SelectionMode.NO_SELECTION) ?
                        (List<EMuleLink>) dlg.getList() :
                        new ArrayList<EMuleLink>();

                if (checked.isEmpty()) {
                    checked.addAll(dlg.getChecked());
                }

                if (!checked.isEmpty()) {
                    log.info("about to startTorrentPartialDownload()");
                    startTorrentPartialDownload(ctxRef.get(), checked);
                    dlg.dismiss();
                }
            }
        }

        private void startTorrentPartialDownload(final Context context, List<EMuleLink> results) {
            if (context == null ||
                !Ref.alive(dlgRef) ||
                results == null ||
                dlgRef.get().getList() == null ||
                results.size() > dlgRef.get().getList().size()) {
                log.warn("can't startTorrentPartialDownload()");
                return;
            }

            final HandpickedCollectionDownloadDialog theDialog = (HandpickedCollectionDownloadDialog) dlgRef.get();

            final boolean[] selection = new boolean[theDialog.getList().size()];
            for (EMuleLink selectedFileEntry : results) {
                //selection[selectedFileEntry.getIndex()] = true;
                log.info("start download {}", selectedFileEntry.getStringValue());
                if (Engine.instance().isStarted()) {
                    Engine.instance().downloadLink(selectedFileEntry);
                }
            }
        }
    }

    private static class NameComparator implements Comparator<EMuleLink> {
        @Override
        public int compare(EMuleLink left, EMuleLink right) {
            return left.getStringValue().compareTo(right.getStringValue());
        }
    }
}
