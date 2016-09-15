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

package org.dkf.jdonkey.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import org.apache.commons.io.FilenameUtils;
import org.dkf.jdonkey.Engine;
import org.dkf.jdonkey.R;
import org.dkf.jdonkey.adapters.SearchResultListAdapter;
import org.dkf.jdonkey.core.ConfigurationManager;
import org.dkf.jdonkey.core.Constants;
import org.dkf.jdonkey.core.MediaType;
import org.dkf.jdonkey.dialogs.NewTransferDialog;
import org.dkf.jdonkey.tasks.StartDownloadTask;
import org.dkf.jdonkey.tasks.Tasks;
import org.dkf.jdonkey.util.UIUtils;
import org.dkf.jdonkey.views.AbstractDialog.OnDialogClickListener;
import org.dkf.jdonkey.views.*;
import org.dkf.jed2k.alert.*;
import org.dkf.jed2k.android.AlertListener;
import org.dkf.jed2k.protocol.server.SharedFileEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author gubatron
 * @author aldenml
 */
public final class SearchFragment extends AbstractFragment implements
        MainFragment,
        OnDialogClickListener,
        SearchProgressView.CurrentQueryReporter,
        AlertListener {
    private static final Logger LOG = LoggerFactory.getLogger(SearchFragment.class);
    private SearchResultListAdapter adapter;

    private SearchInputView searchInput;
    private ProgressBar deepSearchProgress;
    private RichNotification serverConnectionWarning;
    private SearchParametersView searchParametersView;
    private SearchProgressView searchProgress;
    ButtonSearchParametersListener buttonSearchParametersListener;
    private ListView list;
    private String currentQuery;
    private final FileTypeCounter fileTypeCounter;
    private final SparseArray<Byte> toTheRightOf = new SparseArray<>(6);
    private final SparseArray<Byte> toTheLeftOf = new SparseArray<>(6);

    private boolean awaitingResults = false;

    public SearchFragment() {
        super(R.layout.fragment_search);
        buttonSearchParametersListener = new ButtonSearchParametersListener(this);
        fileTypeCounter = new FileTypeCounter();
        currentQuery = null;

        toTheRightOf.put(Constants.FILE_TYPE_AUDIO, Constants.FILE_TYPE_VIDEOS);
        toTheRightOf.put(Constants.FILE_TYPE_VIDEOS, Constants.FILE_TYPE_PICTURES);
        toTheRightOf.put(Constants.FILE_TYPE_PICTURES, Constants.FILE_TYPE_APPLICATIONS);
        toTheRightOf.put(Constants.FILE_TYPE_APPLICATIONS, Constants.FILE_TYPE_DOCUMENTS);
        toTheRightOf.put(Constants.FILE_TYPE_DOCUMENTS, Constants.FILE_TYPE_TORRENTS);
        toTheRightOf.put(Constants.FILE_TYPE_TORRENTS, Constants.FILE_TYPE_AUDIO);
        toTheLeftOf.put(Constants.FILE_TYPE_AUDIO, Constants.FILE_TYPE_TORRENTS);
        toTheLeftOf.put(Constants.FILE_TYPE_VIDEOS, Constants.FILE_TYPE_AUDIO);
        toTheLeftOf.put(Constants.FILE_TYPE_PICTURES, Constants.FILE_TYPE_VIDEOS);
        toTheLeftOf.put(Constants.FILE_TYPE_APPLICATIONS, Constants.FILE_TYPE_PICTURES);
        toTheLeftOf.put(Constants.FILE_TYPE_DOCUMENTS, Constants.FILE_TYPE_APPLICATIONS);
        toTheLeftOf.put(Constants.FILE_TYPE_TORRENTS, Constants.FILE_TYPE_DOCUMENTS);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupAdapter();
        setRetainInstance(true);
    }

    @Override
    public View getHeader(Activity activity) {

        LayoutInflater inflater = LayoutInflater.from(activity);

        View header = inflater.inflate(R.layout.view_search_header, null);

        TextView text = (TextView) header.findViewById(R.id.view_search_header_text_title);
        text.setText(R.string.search);

        ImageButton buttonMenu = (ImageButton) header.findViewById(R.id.view_search_header_more_parameters);
        buttonMenu.setOnClickListener(buttonSearchParametersListener);
        return header;
    }

    @Override
    public void onResume() {
        super.onResume();
        Engine.instance().setListener(this);

        if (adapter != null && (adapter.getCount() > 0 || adapter.getTotalCount() > 0)) {
            refreshFileTypeCounters(true);
        }
    }

    @Override
    public void onDestroy() {
        Engine.instance().removeListener(this);
        super.onDestroy();
    }

    @Override
    public void onShow() {
        warnServerNotConnected(getView());
    }

    @Override
    protected void initComponents(final View view) {
        searchInput = findView(view, R.id.fragment_search_input);
        searchInput.setShowKeyboardOnPaste(true);
        searchInput.setOnSearchListener(new SearchInputOnSearchListener((LinearLayout) view, this));

        deepSearchProgress = findView(view, R.id.fragment_search_deepsearch_progress);
        deepSearchProgress.setVisibility(View.GONE);

        serverConnectionWarning = findView(view, R.id.fragment_search_rating_reminder_notification);
        serverConnectionWarning.setVisibility(View.GONE);

        searchParametersView = findView(view, R.id.fragment_search_parameters);
        searchParametersView.setVisibility(View.GONE);

        /*promotions = findView(view, R.id.fragment_search_promos);
        promotions.setOnPromotionClickListener(new OnPromotionClickListener() {
            @Override
            public void onPromotionClick(PromotionsView v, Slide slide) {
                startPromotionDownload(slide);
            }
        });
*/
        searchProgress = findView(view, R.id.fragment_search_search_progress);
        searchProgress.setCurrentQueryReporter(this);

        searchProgress.setCancelOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelSearch();
            }
        });

        list = findView(view, R.id.fragment_search_list);
        SwipeLayout swipe = findView(view, R.id.fragment_search_swipe);
        swipe.setOnSwipeListener(new SwipeLayout.OnSwipeListener() {
            @Override
            public void onSwipeLeft() {
                switchToThe(true);
            }

            @Override
            public void onSwipeRight() {
                switchToThe(false);
            }
        });

        showSearchView(view);
    }

    private void toggleSearchParametersView() {
        if (searchParametersView.getVisibility() == View.GONE) {
            searchParametersView.setVisibility(View.VISIBLE);
        } else {
            searchParametersView.setVisibility(View.GONE);
        }
    }

    private static final class ButtonSearchParametersListener extends ClickAdapter<SearchFragment> {

        ButtonSearchParametersListener(SearchFragment f) {
            super(f);
        }

        @Override
        public void onClick(SearchFragment f, View v) {
            f.toggleSearchParametersView();
        }
    }

    private void startMagnetDownload(String magnet) {
        UIUtils.showLongMessage(getActivity(), R.string.torrent_url_added);
        //TransferManager.instance().downloadTorrent(magnet,
        //        new HandpickedTorrentDownloadDialogOnFetch(getActivity()));
    }

    private void setupAdapter() {
        if (adapter == null) {
            adapter = new SearchResultListAdapter(getActivity()) {
                @Override
                protected void searchResultClicked(SharedFileEntry sr) {
                    LOG.info("start transfer {}", sr.getFileName());
                    startTransfer(sr, getString(R.string.download_added_to_queue));
                }
            };
        }
        list.setAdapter(adapter);
    }

    private void refreshFileTypeCounters(boolean fileTypeCountersVisible) {
        searchInput.updateFileTypeCounter(Constants.FILE_TYPE_APPLICATIONS, fileTypeCounter.numApplications);
        searchInput.updateFileTypeCounter(Constants.FILE_TYPE_AUDIO, fileTypeCounter.numAudio);
        searchInput.updateFileTypeCounter(Constants.FILE_TYPE_DOCUMENTS, fileTypeCounter.numDocuments);
        searchInput.updateFileTypeCounter(Constants.FILE_TYPE_PICTURES, fileTypeCounter.numPictures);
        searchInput.updateFileTypeCounter(Constants.FILE_TYPE_TORRENTS, fileTypeCounter.numTorrents);
        searchInput.updateFileTypeCounter(Constants.FILE_TYPE_VIDEOS, fileTypeCounter.numVideo);
        searchInput.updateFileTypeCounter(Constants.FILE_TYPE_ARCHIVE, fileTypeCounter.numArchive);
        searchInput.updateFileTypeCounter(Constants.FILE_TYPE_CD_IMAGE, fileTypeCounter.numCDImage);
        searchInput.updateFileTypeCounter(Constants.FILE_TYPE_OTHERS, fileTypeCounter.numOther);
        searchInput.setFileTypeCountersVisible(fileTypeCountersVisible);
    }

    private void performSearch(String query, int mediaTypeId) {
        warnServerNotConnected(getView());
        if (!Engine.instance().getCurrentServerId().isEmpty()) {
            awaitingResults = true;
            adapter.clear();
            adapter.setFileType(mediaTypeId);
            fileTypeCounter.clear();
            refreshFileTypeCounters(false);
            currentQuery = query;
            Engine.instance().performSearch(query);
            searchProgress.setProgressEnabled(true);
            showSearchView(getView());
        }
    }

    public void performSearchMore() {
        if (!Engine.instance().getCurrentServerId().isEmpty()) {
            awaitingResults = true;
            adapter.clear();
            adapter.setFileType(0);
            refreshFileTypeCounters(false);
            Engine.instance().performSearchMore();
            searchProgress.setProgressEnabled(true);
            showSearchView(getView());
        }
    }

    private void cancelSearch() {
        LOG.info("cancel search wait res {}", awaitingResults?"YES":"NO");
        if (awaitingResults) {
            awaitingResults = false;
            adapter.clear();
            fileTypeCounter.clear();
            refreshFileTypeCounters(false);
            currentQuery = null;
            searchProgress.setProgressEnabled(false);
            showSearchView(getView());
        }
    }

    private void searchCompleted(final SearchResultAlert alert) {
        if (awaitingResults) {
            awaitingResults = false;
            adapter.addResults(alert.results);

            // temporary solution, next use filter by hash to support related search
            for (SharedFileEntry entry : alert.results.files) {
                fileTypeCounter.increment(MediaType.getMediaTypeForExtension(FilenameUtils.getExtension(entry.getFileName())));
            }
        }

        // TODO - fix it!
        adapter.setFileType(0);

        refreshFileTypeCounters(true);
        searchProgress.setProgressEnabled(false);
        showSearchView(getView());
    }

    private void showSearchView(View view) {
        if (awaitingResults) {
            switchView(view, R.id.fragment_search_search_progress);
        } else {
            switchView(view, R.id.fragment_search_list);
        }
    }

    private void switchView(View v, int id) {
        if (v != null) {
            FrameLayout frameLayout = findView(v, R.id.fragment_search_framelayout);
            int childCount = frameLayout.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = frameLayout.getChildAt(i);
                childAt.setVisibility((childAt.getId() == id) ? View.VISIBLE : View.INVISIBLE);
            }
        }
    }

    @Override
    public void onDialogClick(String tag, int which) {
        if (tag.equals(NewTransferDialog.TAG) && which == Dialog.BUTTON_POSITIVE) {
            startDownload(this.getActivity(), NewTransferDialog.entry, getString(R.string.download_added_to_queue));
            NewTransferDialog.entry = null;
        }
    }

    private void startTransfer(final SharedFileEntry sr, final String toastMessage) {
        if (ConfigurationManager.instance().getBoolean(Constants.PREF_KEY_GUI_SHOW_NEW_TRANSFER_DIALOG)) {
            try {
                NewTransferDialog dlg = NewTransferDialog.newInstance(sr, false);
                dlg.show(getFragmentManager());
            } catch (IllegalStateException e) {
                // android.app.FragmentManagerImpl.checkStateLoss:1323 -> java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
                // just start the download then if the dialog crapped out.
                onDialogClick(NewTransferDialog.TAG, Dialog.BUTTON_POSITIVE);
            }
        } else {
            startDownload(getActivity(), sr, toastMessage);
        }
    }

    public static void startDownload(Context ctx, SharedFileEntry entry, String message) {
        StartDownloadTask task = new StartDownloadTask(ctx, entry, null, message);
        Tasks.executeParallel(task);
    }

    private void warnServerNotConnected(View v) {
        if (Engine.instance().getCurrentServerId().isEmpty()) {
            LOG.info("server is not connected");
            serverConnectionWarning.setVisibility(View.VISIBLE);
        } else {
            LOG.info("server connected");
            serverConnectionWarning.setVisibility(View.GONE);
        }
    }

    // takes user to Google Play store so it can rate the app.
    ///private ClickAdapter<SearchFragment> createOnRateClickAdapter(final RichNotification ratingReminder, final ConfigurationManager CM) {
     //   return new OnRateClickAdapter(SearchFragment.this, ratingReminder, CM);
    //}

    // opens default email client and pre-fills email to support@frostwire.com
    // with some information about the app and environment.
    //private ClickAdapter<SearchFragment> createOnFeedbackClickAdapter(final RichNotification ratingReminder, final ConfigurationManager CM) {
    //    return new OnFeedbackClickAdapter(SearchFragment.this, ratingReminder, CM);
    //}
/*
    private void startPromotionDownload(Slide slide) {
        SearchResult sr;

        switch (slide.method) {
            case Slide.DOWNLOAD_METHOD_TORRENT:
                sr = new TorrentPromotionSearchResult(slide);
                break;
            case Slide.DOWNLOAD_METHOD_HTTP:
                sr = new HttpSlideSearchResult(slide);
                break;
            default:
                sr = null;
                break;
        }
        if (sr == null) {
            //check if there is a URL available to open a web browser.
            if (slide.clickURL != null) {
                Intent i = new Intent("android.intent.action.VIEW", Uri.parse(slide.clickURL));
                try {
                    getActivity().startActivity(i);
                } catch (Throwable t) {
                    // some devices incredibly may have no apps to handle this intent.
                }
            }

            return;
        }

        String stringDownloadingPromo;

        try {
            stringDownloadingPromo = getString(R.string.downloading_promotion, sr.getDisplayName());
        } catch (Throwable e) {
            stringDownloadingPromo = getString(R.string.azureus_manager_item_downloading);
        }

        startTransfer(sr, stringDownloadingPromo);
    }

    private void uxLogAction(SearchResult sr) {
        UXStats.instance().log(UXAction.SEARCH_RESULT_CLICKED);

        if (sr instanceof HttpSearchResult) {
            UXStats.instance().log(UXAction.DOWNLOAD_CLOUD_FILE);
        } else if (sr instanceof TorrentSearchResult) {
            if (sr instanceof TorrentCrawledSearchResult) {
                UXStats.instance().log(UXAction.DOWNLOAD_PARTIAL_TORRENT_FILE);
            } else {
                UXStats.instance().log(UXAction.DOWNLOAD_FULL_TORRENT_FILE);
            }
        }
    }
*/
    @Override
    public String getCurrentQuery() {
        return currentQuery;
    }

    private void switchToThe(boolean right) {
        if (adapter == null) {
            return;
        }
        final byte currentFileType = (byte) adapter.getFileType();
        if (currentFileType != -1) { // SearchResultListAdapter#NO_FILE_TYPE (refactor this)
            final byte nextFileType = (right) ? toTheRightOf.get(currentFileType) : toTheLeftOf.get(currentFileType);
            searchInput.performClickOnRadioButton(nextFileType);
        }
    }

    @Override
    public void onListen(ListenAlert alert) {

    }

    @Override
    public void onSearchResult(final SearchResultAlert alert) {
        LOG.info("search result size {} more {}", alert.results.files.size(), alert.results.hasMoreResults()?"YES":"NO");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                searchCompleted(alert);
            }
        });
    }

    @Override
    public void onServerConnectionAlert(ServerConnectionAlert alert) {

    }

    @Override
    public void onServerMessage(ServerMessageAlert alert) {

    }

    @Override
    public void onServerStatus(ServerStatusAlert alert) {

    }

    @Override
    public void onServerIdAlert(ServerIdAlert alert) {

    }

    @Override
    public void onServerConnectionClosed(ServerConectionClosed alert) {

    }

    private static class SearchInputOnSearchListener implements SearchInputView.OnSearchListener {
        private final LinearLayout parentView;
        private final SearchFragment fragment;

        SearchInputOnSearchListener(LinearLayout parentView, SearchFragment fragment) {
            this.parentView = parentView;
            this.fragment = fragment;
        }

        public void onSearch(View v, String query, int mediaTypeId) {
            fragment.performSearch(query, mediaTypeId);
        }

        public void onMediaTypeSelected(View view, int mediaTypeId) {
            fragment.adapter.setFileType(mediaTypeId);
            fragment.showSearchView(parentView);
        }

        public void onClear(View v) {
            fragment.cancelSearch();
        }
    }
/*
    private static class LoadSlidesTask extends AsyncTask<Void, Void, List<Slide>> {

        private final WeakReference<SearchFragment> fragment;

        LoadSlidesTask(SearchFragment fragment) {
            this.fragment = new WeakReference<>(fragment);
        }

        @Override
        protected List<Slide> doInBackground(Void... params) {
            try {
                HttpClient http = HttpClientFactory.getInstance(HttpClientFactory.HttpContext.SEARCH);
                String url = String.format("%s?from=android&fw=%s&sdk=%s", Constants.SERVER_PROMOTIONS_URL, Constants.FROSTWIRE_VERSION_STRING, Build.VERSION.SDK_INT);
                String json = http.get(url);
                SlideList slides = JsonUtils.toObject(json, SlideList.class);
                // yes, these requests are done only once per session.
                //LOG.info("SearchFragment.LoadSlidesTask performed http request to " + url);
                return slides.slides;
            } catch (Throwable e) {
                LOG.error("Error loading slides from url", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Slide> result) {
            SearchFragment f = fragment.get();
            if (f != null && result != null && !result.isEmpty()) {
                f.slides = result;
                f.promotions.setSlides(result);
            }
        }
    }
*/
    private static final class FileTypeCounter {
        public int numAudio = 0;
        public int numVideo = 0;
        public int numPictures  = 0;
        public int numApplications  = 0;
        public int numDocuments = 0;
        public int numTorrents  = 0;
        public int numArchive = 0;
        public int numCDImage = 0;
        public int numOther = 0;

        private void increment(MediaType mt) {
            if (mt != null) {
                switch (mt.getId()) {
                    case Constants.FILE_TYPE_AUDIO:
                        numAudio++;
                        break;
                    case Constants.FILE_TYPE_VIDEOS:
                        numVideo++;
                        break;
                    case Constants.FILE_TYPE_PICTURES:
                        numPictures++;
                        break;
                    case Constants.FILE_TYPE_APPLICATIONS:
                        numApplications++;
                        break;
                    case Constants.FILE_TYPE_DOCUMENTS:
                        numDocuments++;
                        break;
                    case Constants.FILE_TYPE_TORRENTS:
                        numTorrents++;
                        break;
                    case Constants.FILE_TYPE_ARCHIVE:
                        numArchive++;
                        break;
                    case Constants.FILE_TYPE_CD_IMAGE:
                        numCDImage++;
                        break;
                    default:
                        numOther++;
                        break;
                }
            }
            else {
                numOther++;
            }
        }

        public void clear() {
            this.numAudio = 0;
            this.numApplications = 0;
            this.numDocuments = 0;
            this.numPictures = 0;
            this.numTorrents = 0;
            this.numVideo = 0;
            this.numArchive = 0;
            this.numCDImage = 0;
            this.numOther = 0;
        }
    }
/*
    private static class OnRateClickAdapter extends ClickAdapter<SearchFragment> {
        private final WeakReference<RichNotification> ratingReminderRef;
        private final ConfigurationManager CM;

        OnRateClickAdapter(final SearchFragment owner, final RichNotification ratingReminder, final ConfigurationManager CM) {
            super(owner);
            ratingReminderRef = Ref.weak(ratingReminder);
            this.CM = CM;
        }

        @Override
        public void onClick(SearchFragment owner, View v) {
            if (Ref.alive(ratingReminderRef)) {
                ratingReminderRef.get().setVisibility(View.GONE);
            }
            CM.setBoolean(Constants.PREF_KEY_GUI_ALREADY_RATED_US_IN_MARKET, true);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + Constants.APP_PACKAGE_NAME));
            try {
                owner.startActivity(intent);
            } catch (Throwable ignored) {
            }
        }
    }

    private static class OnFeedbackClickAdapter extends ClickAdapter<SearchFragment> {
        private final WeakReference<RichNotification> ratingReminderRef;
        private final ConfigurationManager CM;

        OnFeedbackClickAdapter(SearchFragment owner, final RichNotification ratingReminder, final ConfigurationManager CM) {
            super(owner);
            ratingReminderRef = Ref.weak(ratingReminder);
            this.CM = CM;
        }

        @Override
        public void onClick(SearchFragment owner, View v) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@frostwire.com"});
            String plusOrBasic = (Constants.IS_GOOGLE_PLAY_DISTRIBUTION) ? "basic" : "plus";
            intent.putExtra(Intent.EXTRA_SUBJECT, String.format("[Feedback - frostwire-android (%s) - v%s b%s]", plusOrBasic, Constants.FROSTWIRE_VERSION_STRING, Constants.FROSTWIRE_BUILD));

            String body = String.format("\n\nAndroid SDK: %d\nAndroid RELEASE: %s (%s)\nManufacturer-Model: %s - %s\nDevice: %s\nBoard: %s\nCPU ABI: %s\nCPU ABI2: %s\n\n",
                    Build.VERSION.SDK_INT,
                    Build.VERSION.RELEASE,
                    Build.VERSION.CODENAME,
                    Build.MANUFACTURER,
                    Build.MODEL,
                    Build.DEVICE,
                    Build.BOARD,
                    Build.CPU_ABI,
                    Build.CPU_ABI2);

            intent.putExtra(Intent.EXTRA_TEXT, body);
            owner.startActivity(Intent.createChooser(intent, owner.getString(R.string.choose_email_app)));

            if (Ref.alive(ratingReminderRef)) {
                ratingReminderRef.get().setVisibility(View.GONE);
            }
            CM.setBoolean(Constants.PREF_KEY_GUI_ALREADY_RATED_US_IN_MARKET, true);
        }
    }
    */
}
