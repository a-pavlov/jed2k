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

package org.dkf.jmule.adapters;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import org.apache.commons.io.FilenameUtils;
import org.dkf.jmule.Constants;
import org.dkf.jmule.MediaType;
import org.dkf.jed2k.protocol.SearchEntry;
import org.dkf.jed2k.util.Ref;
import org.dkf.jmule.Engine;
import org.dkf.jmule.R;
import org.dkf.jmule.activities.MainActivity;
import org.dkf.jmule.adapters.menu.BlockSearchAction;
import org.dkf.jmule.adapters.menu.SearchMoreAction;
import org.dkf.jmule.util.UIUtils;
import org.dkf.jmule.views.AbstractListAdapter;
import org.dkf.jmule.views.ClickAdapter;
import org.dkf.jmule.views.MenuAction;
import org.dkf.jmule.views.MenuAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author gubatron
 * @author aldenml
 */
public abstract class SearchResultListAdapter extends AbstractListAdapter<SearchEntry> {

    private static final Logger log = LoggerFactory.getLogger(SearchResultListAdapter.class);
    private static final int NO_FILE_TYPE = -1;

    private final OnLinkClickListener linkListener;
    private final PreviewClickListener previewClickListener;
    private boolean moreResults = false;
    private Comparator<SearchEntry> sourcesCountComparator = new SourcesCountComparator();

    private int fileType;

    protected SearchResultListAdapter(Context context) {
        super(context, R.layout.view_bittorrent_search_result_list_item);
        this.linkListener = new OnLinkClickListener();
        this.previewClickListener = new PreviewClickListener(context, this);
        this.fileType = NO_FILE_TYPE;
    }

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
        filter();
    }

    public void addResults(final List<SearchEntry> entries, boolean hasMoreResults) {
        log.debug("results {} more {}", entries.size(), hasMoreResults?"yes":"no");
        moreResults = hasMoreResults;
        list.addAll(entries);
        Collections.sort(list, Collections.reverseOrder(sourcesCountComparator));
        visualList.addAll(list);
        notifyDataSetChanged();
    }


    @Override
    protected void populateView(View view, final SearchEntry entry) {
        maybeMarkTitleOpened(view, entry);
        populateFilePart(view, entry);
    }

    public void removeEntry(SearchEntry searchEntry) {
        boolean removed = list.remove(searchEntry);
        boolean removed2 = visualList.remove(searchEntry);
        log.info("item blocked {} / {}", removed, removed2);
        notifyDataSetChanged();
    }

    private void maybeMarkTitleOpened(View view, SearchEntry se) {
        int clickedColor = getContext().getResources().getColor(R.color.browse_peer_listview_item_inactive_foreground);
        int unclickedColor = getContext().getResources().getColor(R.color.app_text_primary);
        TextView title = findView(view, R.id.view_bittorrent_search_result_list_item_title);
        //title.setTextColor(LocalSearchEngine.instance().hasBeenOpened(sr) ? clickedColor : unclickedColor);
    }

    private void populateFilePart(View view, final SearchEntry entry) {
        TextView adIndicator = findView(view, R.id.view_bittorrent_search_result_list_item_ad_indicator);
        adIndicator.setVisibility(View.GONE);

        TextView title = findView(view, R.id.view_bittorrent_search_result_list_item_title);
        title.setText(entry.getFileName());
        if (Engine.instance().hasTransfer(entry.getHash())) {
            title.setTextColor(ContextCompat.getColor(view.getContext(), R.color.warning_red));
        } else {
            title.setTextColor(ContextCompat.getColor(view.getContext(), R.color.app_text_primary));
        }

        TextView fileSize = findView(view, R.id.view_bittorrent_search_result_list_item_file_size);
        if (entry.getFileSize() > 0) {
            fileSize.setText(UIUtils.getBytesInHuman(entry.getFileSize()));
        } else {
            fileSize.setText("...");
        }

        TextView extra = findView(view, R.id.view_bittorrent_search_result_list_item_text_extra);
        extra.setText(FilenameUtils.getExtension(entry.getFileName()));

        TextView seeds = findView(view, R.id.view_bittorrent_search_result_list_item_text_seeds);
        String strSeeds = view.getContext().getResources().getString(R.string.search_item_sources);
        seeds.setText(String.format(strSeeds, entry.getSources()));
        TextView completeSources = findView(view, R.id.view_bittorrent_search_result_list_item_text_comp_percent);
        String formatCompleteSources = getContext().getString(R.string.complete_sources);
        completeSources.setText(String.format(formatCompleteSources, entry.getSources()!=0?entry.getCompleteSources()*100/entry.getSources():0) + "%");

        TextView sourceLink = findView(view, R.id.view_bittorrent_search_result_list_item_text_source);

        if (entry.getSource() == SearchEntry.SOURCE_SERVER) {
            sourceLink.setText(R.string.search_item_source_server);
        } else {
            sourceLink.setText(R.string.search_source_type_dht);
        }
    }

    @Override
    protected void onItemClicked(View v) {
        SearchEntry se = (SearchEntry) v.getTag();
        if (!Engine.instance().hasTransfer(se.getHash())) {
            searchResultClicked(se);
        }
    }

    abstract protected void searchResultClicked(SearchEntry se);

    private void filter() {
        this.visualList = filter(list);
        notifyDataSetInvalidated();
    }

    public List<SearchEntry> filter(List<SearchEntry> results) {
        ArrayList<SearchEntry> l = new ArrayList<>();
        for (SearchEntry se : results) {
            MediaType mt;
            String extension = FilenameUtils.getExtension(se.getFileName());
            mt = MediaType.getMediaTypeForExtension(extension);

            if (accept(se, mt)) {
                l.add(se);
            }
        }

        return l;
    }

    private boolean accept(SearchEntry se, MediaType mt) {
        return (mt != null && mt.getId() == fileType) ||
                (mt == null && fileType == Constants.FILE_TYPE_OTHERS);
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

            SearchEntry entry = (SearchEntry) v.getTag();
            log.info("request preview for {}", entry);
        }
    }

    void populateMenuActions(SearchEntry entry, List<MenuAction> actions) {
        // search more is available only on server source
        if (entry.getSource() != SearchEntry.SOURCE_SERVER) return;
        // HACK!
        // TODO - replace it with appropriate solution
        MainActivity a = (MainActivity)getContext();
        if (a != null) {
            actions.add(new SearchMoreAction(getContext(), a.getSearchFragment()));
            actions.add(new BlockSearchAction(getContext(), a.getSearchFragment(), entry));
        }
    }

    protected MenuAdapter getMenuAdapter(View view) {
        Object tag = view.getTag();
        String title = "";
        List<MenuAction> items = new ArrayList<>();
        populateMenuActions((SearchEntry)tag, items);
        return items.size() > 0 ? new MenuAdapter(view.getContext(), title, items) : null;
    }

    private static final class SourcesCountComparator implements Comparator<SearchEntry> {
        public int compare(final SearchEntry lhs, SearchEntry rhs) {
            try {
                return Integer.signum(lhs.getSources() - rhs.getSources());
            } catch (Exception e) {
                // ignore, not really super important
            }
            return 0;
        }
    }
}
