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

package org.dkf.jdonkey.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import org.apache.commons.io.FilenameUtils;
import org.dkf.jdonkey.R;
import org.dkf.jdonkey.activities.MainActivity;
import org.dkf.jdonkey.adapters.menu.SearchMoreAction;
import org.dkf.jdonkey.core.Constants;
import org.dkf.jdonkey.core.MediaType;
import org.dkf.jdonkey.util.UIUtils;
import org.dkf.jdonkey.views.*;
import org.dkf.jed2k.protocol.server.SharedFileEntry;
import org.dkf.jed2k.protocol.server.search.SearchResult;
import org.dkf.jed2k.util.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gubatron
 * @author aldenml
 */
public abstract class SearchResultListAdapter extends AbstractListAdapter<SharedFileEntry> {

    private static final Logger log = LoggerFactory.getLogger(SearchResultListAdapter.class);
    private static final int NO_FILE_TYPE = -1;

    private final OnLinkClickListener linkListener;
    private final PreviewClickListener previewClickListener;
    private boolean moreResults = false;

    private int fileType;

    //private ImageLoader thumbLoader;

    protected SearchResultListAdapter(Context context) {
        super(context, R.layout.view_bittorrent_search_result_list_item);
        this.linkListener = new OnLinkClickListener();
        this.previewClickListener = new PreviewClickListener(context, this);
        this.fileType = NO_FILE_TYPE;
        //this.thumbLoader = ImageLoader.getInstance(context);
    }

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
        filter();
    }

    public void addResults(final SearchResult sr) {
        log.debug("results {} more {}", sr.files.size(), sr.hasMoreResults()?"yes":"no");
        moreResults = sr.hasMoreResults();
        visualList.addAll(sr.files);
        list.addAll(sr.files);
        notifyDataSetChanged();
    }


    @Override
    protected void populateView(View view, final SharedFileEntry entry) {
        maybeMarkTitleOpened(view, entry);
        populateFilePart(view, entry);
        //populateThumbnail(view, sr);
    }

    private void maybeMarkTitleOpened(View view, SharedFileEntry sr) {
        int clickedColor = getContext().getResources().getColor(R.color.browse_peer_listview_item_inactive_foreground);
        int unclickedColor = getContext().getResources().getColor(R.color.app_text_primary);
        TextView title = findView(view, R.id.view_bittorrent_search_result_list_item_title);
        //title.setTextColor(LocalSearchEngine.instance().hasBeenOpened(sr) ? clickedColor : unclickedColor);
    }

    private void populateFilePart(View view, final SharedFileEntry entry) {
        ImageView fileTypeIcon = findView(view, R.id.view_bittorrent_search_result_list_item_filetype_icon);
        fileTypeIcon.setImageResource(getFileTypeIconId());

        TextView adIndicator = findView(view, R.id.view_bittorrent_search_result_list_item_ad_indicator);
        adIndicator.setVisibility(View.GONE);

        TextView title = findView(view, R.id.view_bittorrent_search_result_list_item_title);
        title.setText(entry.getFileName());

        TextView fileSize = findView(view, R.id.view_bittorrent_search_result_list_item_file_size);
        if (entry.getFileSize() > 0) {
            fileSize.setText(UIUtils.getBytesInHuman(entry.getFileSize()));
        } else {
            fileSize.setText("...");
        }

        TextView extra = findView(view, R.id.view_bittorrent_search_result_list_item_text_extra);
        extra.setText(FilenameUtils.getExtension(entry.getFileName()));

        TextView seeds = findView(view, R.id.view_bittorrent_search_result_list_item_text_seeds);
        seeds.setText(Integer.toString(entry.getCompleteSources()));

        TextView sourceLink = findView(view, R.id.view_bittorrent_search_result_list_item_text_source);
        sourceLink.setText(entry.hash.toString()); // TODO: ask for design
        sourceLink.setTag(entry.getFileName());
        sourceLink.setPaintFlags(sourceLink.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        sourceLink.setOnClickListener(linkListener);
    }

    private void populateThumbnail(View view, SharedFileEntry sr) {
        SearchThumbnailImageView fileTypeIcon = findView(view, R.id.view_bittorrent_search_result_list_item_filetype_icon);
        //if (sr.getThumbnailUrl() != null) {
        //    thumbLoader.load(Uri.parse(sr.getThumbnailUrl()), fileTypeIcon, 96, 96, getFileTypeIconId());
        //}

        fileTypeIcon.setOnClickListener(previewClickListener);
        //if (isAudio(sr) || sr instanceof YouTubePackageSearchResult) {
        //    fileTypeIcon.setTag(sr);
        //    fileTypeIcon.setOverlayState(MediaPlaybackOverlay.MediaPlaybackState.PREVIEW);
        //} else {
            fileTypeIcon.setTag(null);
            fileTypeIcon.setOverlayState(MediaPlaybackOverlay.MediaPlaybackState.NONE);
        //}
    }

    @Override
    protected void onItemClicked(View v) {
        SharedFileEntry sr = (SharedFileEntry) v.getTag();
        searchResultClicked(sr);
    }

    abstract protected void searchResultClicked(SharedFileEntry sr);

    private void filter() {
        this.visualList = filter(list);
        notifyDataSetInvalidated();
    }

    public List<SharedFileEntry> filter(List<SharedFileEntry> results) {
        ArrayList<SharedFileEntry> l = new ArrayList<>();
        for (SharedFileEntry sr : results) {
            MediaType mt;
            String extension = FilenameUtils.getExtension(sr.getFileName());
            mt = MediaType.getMediaTypeForExtension(extension);

            if (accept(sr, mt)) {
                l.add(sr);
            }
        }

        return l;
    }

    private boolean accept(SharedFileEntry sr, MediaType mt) {
        return mt != null && mt.getId() == fileType;
    }

    private int getFileTypeIconId() {
        switch (fileType) {
            case Constants.FILE_TYPE_APPLICATIONS:
                return R.drawable.list_item_application_icon;
            case Constants.FILE_TYPE_AUDIO:
                return R.drawable.list_item_audio_icon;
            case Constants.FILE_TYPE_DOCUMENTS:
                return R.drawable.list_item_document_icon;
            case Constants.FILE_TYPE_PICTURES:
                return R.drawable.list_item_picture_icon;
            case Constants.FILE_TYPE_VIDEOS:
                return R.drawable.list_item_video_icon;
            case Constants.FILE_TYPE_TORRENTS:
                return R.drawable.list_item_torrent_icon;
            default:
                return R.drawable.list_item_question_mark;
        }
    }

    private static class OnLinkClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            String url = (String) v.getTag();
            UIUtils.openURL(v.getContext(), url);
        }
    }

    private static final class PreviewClickListener extends ClickAdapter<Context> {
        final WeakReference<SearchResultListAdapter> adapterRef;

        PreviewClickListener(Context ctx, SearchResultListAdapter adapter) {
            super(ctx);
            adapterRef = Ref.weak(adapter);
        }

        @Override
        public void onClick(Context ctx, View v) {
            if (v == null) {
                return;
            }

            SharedFileEntry entry = (SharedFileEntry) v.getTag();
            log.info("request preview for {}", entry);
        }
    }

    void populateMenuActions(SharedFileEntry entry, List<MenuAction> actions) {
        // HACK!
        // TODO - replace it with appropriate solution
        MainActivity a = (MainActivity)getContext();
        if (a != null) {
            actions.add(new SearchMoreAction(getContext(), a.getSearchFragment()));
        }
    }

    protected MenuAdapter getMenuAdapter(View view) {
        Object tag = view.getTag();
        String title = "";
        List<MenuAction> items = new ArrayList<>();
        populateMenuActions((SharedFileEntry)tag, items);
        return items.size() > 0 ? new MenuAdapter(view.getContext(), title, items) : null;
    }
}
