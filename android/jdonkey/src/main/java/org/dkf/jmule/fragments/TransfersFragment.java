/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2016, FrostWire(R). All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dkf.jmule.fragments;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import org.dkf.jed2k.Pair;
import org.dkf.jmule.ConfigurationManager;
import org.dkf.jmule.Constants;
import org.dkf.jed2k.util.Ref;
import org.dkf.jmule.Engine;
import org.dkf.jmule.R;
import org.dkf.jmule.activities.SettingsActivity;
import org.dkf.jmule.adapters.TransferListAdapter;
import org.dkf.jmule.dialogs.MenuDialog;
import org.dkf.jmule.transfers.Transfer;
import org.dkf.jmule.transfers.TransferManager;
import org.dkf.jmule.util.SystemUtils;
import org.dkf.jmule.util.UIUtils;
import org.dkf.jmule.views.AbstractDialog.OnDialogClickListener;
import org.dkf.jmule.views.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * @author gubatron
 * @author aldenml
 */
public class TransfersFragment extends AbstractFragment implements TimerObserver, MainFragment, OnDialogClickListener {
    private static final Logger LOG = LoggerFactory.getLogger(TransfersFragment.class);
    private static final String SELECTED_STATUS_STATE_KEY = "selected_status";
    private static final int UI_UPDATE_INTERVAL_IN_SECS = 2;
    private static final int DHT_STATUS_UPDATE_INTERVAL_IN_SECS = 10;

    private final Comparator<Transfer> transferComparator;
    private final ButtonAddTransferListener buttonAddTransferListener;
    private final ButtonMenuListener buttonMenuListener;
    private Button buttonSelectAll;
    private Button buttonSelectDownloading;
    private Button buttonSelectCompleted;
    private ExpandableListView list;
    private TextView textDownloads;
    private TextView textUploads;
    private TextView textDht;
    private ClearableEditTextView addTransferUrlTextView;
    private TransferListAdapter adapter;
    private TransferStatus selectedStatus;
    private TimerSubscription subscription;
    private int delayedDHTUpdateTimeElapsed = 0;
    private boolean isVPNactive;
    private static boolean firstTimeShown = true;
    private Handler vpnRichToastHandler;
    private int totalDhtNodes = -1;

    private boolean showTorrentSettingsOnClick;

    public TransfersFragment() {
        super(R.layout.fragment_transfers);
        this.transferComparator = new TransferComparator();
        this.buttonAddTransferListener = new ButtonAddTransferListener(this);
        this.buttonMenuListener = new ButtonMenuListener(this);
        selectedStatus = TransferStatus.ALL;
        vpnRichToastHandler = new Handler();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            //selectedStatus = TransferStatus.valueOf(savedInstanceState.getString(SELECTED_STATUS_STATE_KEY, TransferStatus.ALL.name()));
        }
        addTransferUrlTextView = findView(getView(), R.id.fragment_transfers_add_transfer_text_input);
        addTransferUrlTextView.replaceSearchIconDrawable(R.drawable.clearable_edittext_add_icon);
        addTransferUrlTextView.setFocusable(true);
        addTransferUrlTextView.setFocusableInTouchMode(true);
        addTransferUrlTextView.setOnKeyListener(new AddTransferTextListener(this));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        subscription = TimerService.subscribe(this, UI_UPDATE_INTERVAL_IN_SECS);
    }

    @Override
    public void onResume() {
        super.onResume();
        initStorageRelatedRichNotifications(getView());
        onTime();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        subscription.unsubscribe();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putString(SELECTED_STATUS_STATE_KEY, selectedStatus.name());
    }

    @Override
    public void onPause() {
        super.onPause();
        if (adapter != null) {
            adapter.dismissDialogs();
        }
    }

    @Override
    public void onTime() {
        if (adapter != null) {
            List<Transfer> transfers = filter(TransferManager.instance().getTransfers(), selectedStatus);
            Collections.sort(transfers, transferComparator);
            adapter.updateList(transfers);
        } else if (this.getActivity() != null) {
            setupAdapter();
        }

        //  format strings
        Pair<Long, Long> dub = Engine.instance().getDownloadUploadBandwidth();
        String sDown = UIUtils.rate2speed(dub.left / 1024);
        String sUp = UIUtils.rate2speed(dub.right / 1024);

        // number of uploads (seeding) and downloads
        int downloads = TransferManager.instance().getActiveDownloads();
        int uploads = TransferManager.instance().getActiveUploads();

        delayedDHTUpdateTimeElapsed += UI_UPDATE_INTERVAL_IN_SECS;

        if (delayedDHTUpdateTimeElapsed >= DHT_STATUS_UPDATE_INTERVAL_IN_SECS) {
            delayedDHTUpdateTimeElapsed = 0;
            totalDhtNodes = Engine.instance().getTotalDhtNodes();
        }

        updateStatusBar(sDown, sUp, downloads, uploads);
    }

    private void updateStatusBar(String sDown, String sUp, int downloads, int uploads) {
        textDownloads.setText(downloads + " @ " + sDown);
        textUploads.setText(uploads + " @ " + sUp);
        textDht.setText(getString(R.string.dht_nodes, (totalDhtNodes>=0)?new Integer(totalDhtNodes).toString():"???"));
    }

    @Override
    public View getHeader(Activity activity) {
        LayoutInflater inflater = LayoutInflater.from(activity);

        View header = inflater.inflate(R.layout.view_transfers_header, null);

        TextView text = (TextView) header.findViewById(R.id.view_transfers_header_text_title);
        text.setText(R.string.transfers);

        ImageButton buttonMenu = (ImageButton) header.findViewById(R.id.view_transfers_header_button_menu);
        buttonMenu.setOnClickListener(buttonMenuListener);

        ImageButton buttonAddTransfer = (ImageButton) header.findViewById(R.id.view_transfers_header_button_add_transfer);
        buttonAddTransfer.setOnClickListener(buttonAddTransferListener);

        return header;
    }

    public void selectStatusTabToThe(boolean right) {
        final TransferStatus[] allStatusesArray = TransferStatus.getAllStatusesArray();
        int currentStatusIndex = -1;
        for (int i = 0; i < allStatusesArray.length; i++) {
            if (selectedStatus == allStatusesArray[i]) {
                currentStatusIndex = i;
                break;
            }
        }
        if (currentStatusIndex != -1) {
            int increment = (right) ? 1 : -1;
            currentStatusIndex = (currentStatusIndex + increment) % allStatusesArray.length;
            if (currentStatusIndex < 0) {
                currentStatusIndex = allStatusesArray.length - 1;
            }
            selectStatusTab(allStatusesArray[currentStatusIndex]);
        }
    }

    public void selectStatusTab(TransferStatus status) {
        selectedStatus = status;
        switch (selectedStatus) {
            case ALL:
                buttonSelectAll.performClick();
                break;
            case DOWNLOADING:
                buttonSelectDownloading.performClick();
                break;
            case COMPLETED:
                buttonSelectCompleted.performClick();
                break;
        }
    }

    @Override
    public void onShow() {
        if (firstTimeShown) {
            firstTimeShown = false;
        }
    }

    @Override
    protected void initComponents(View v) {
        initStorageRelatedRichNotifications(v);

        buttonSelectAll = findView(v, R.id.fragment_transfers_button_select_all);
        buttonSelectAll.setOnClickListener(new ButtonTabListener(this, TransferStatus.ALL));

        buttonSelectDownloading = findView(v, R.id.fragment_transfers_button_select_downloading);
        buttonSelectDownloading.setOnClickListener(new ButtonTabListener(this, TransferStatus.DOWNLOADING));

        buttonSelectCompleted = findView(v, R.id.fragment_transfers_button_select_completed);
        buttonSelectCompleted.setOnClickListener(new ButtonTabListener(this, TransferStatus.COMPLETED));

        list = findView(v, R.id.fragment_transfers_list);
        SwipeLayout swipe = findView(v, R.id.fragment_transfers_swipe);
        swipe.setOnSwipeListener(new SwipeLayout.OnSwipeListener() {
            @Override
            public void onSwipeLeft() {
                selectStatusTabToThe(true);
            }

            @Override
            public void onSwipeRight() {
                selectStatusTabToThe(false);
            }
        });

        textDownloads = findView(v, R.id.fragment_transfers_text_downloads);
        textUploads = findView(v, R.id.fragment_transfers_text_uploads);
        textDht = findView(v, R.id.fragment_transfers_dht);
        textDht.setText(getString(R.string.dht_nodes, "???"));
    }

    public void initStorageRelatedRichNotifications(View v) {
        if (v == null) {
            v = getView();
        }
        RichNotification sdCardNotification = findView(v, R.id.fragment_transfers_sd_card_notification);
        sdCardNotification.setVisibility(View.GONE);

        RichNotification internalMemoryNotification = findView(v, R.id.fragment_transfers_internal_memory_notification);
        internalMemoryNotification.setVisibility(View.GONE);

        if (TransferManager.isUsingSDCardPrivateStorage() && !sdCardNotification.wasDismissed()) {
            String currentPath = ConfigurationManager.instance().getStoragePath();
            boolean inPrivateFolder = currentPath.contains("Android/data");

            if (inPrivateFolder) {
                sdCardNotification.setVisibility(View.VISIBLE);
                sdCardNotification.setOnClickListener(new SDCardNotificationListener(this));
            }
        }

        //if you do have an SD Card mounted and you're using internal memory, we'll let you know
        //that you now can use the SD Card. We'll keep this for a few releases.
        File sdCardDir = getBiggestSDCardDir(getActivity());
        if (sdCardDir != null && SystemUtils.isSecondaryExternalStorageMounted(sdCardDir) &&
                !TransferManager.isUsingSDCardPrivateStorage() &&
                !internalMemoryNotification.wasDismissed()) {
            String bytesAvailableInHuman = UIUtils.getBytesInHuman(SystemUtils.getAvailableStorageSize(sdCardDir));
            String internalMemoryNotificationDescription = getString(R.string.saving_to_internal_memory_description, bytesAvailableInHuman);
            internalMemoryNotification.setDescription(internalMemoryNotificationDescription);
            internalMemoryNotification.setVisibility(View.VISIBLE);
            internalMemoryNotification.setOnClickListener(new SDCardNotificationListener(this));
        }

    }

    private void setupAdapter() {
        List<Transfer> transfers = filter(TransferManager.instance().getTransfers(), selectedStatus);
        Collections.sort(transfers, transferComparator);
        adapter = new TransferListAdapter(TransfersFragment.this.getActivity(), transfers);
        list.setAdapter(adapter);
    }

    private List<Transfer> filter(List<Transfer> transfers, TransferStatus status) {
        Iterator<Transfer> it;

        switch (status) { // replace this filter by a more functional style
            case DOWNLOADING:
                it = transfers.iterator();
                while (it.hasNext()) {
                    if (it.next().isComplete()) {
                        it.remove();
                    }
                }
                return transfers;
            case COMPLETED:
                it = transfers.iterator();
                while (it.hasNext()) {
                    if (!it.next().isComplete()) {
                        it.remove();
                    }
                }
                return transfers;
            default:
                return transfers;
        }
    }

    private static final String TRANSFERS_DIALOG_ID = "transfers_dialog";

    private static final int CLEAR_MENU_DIALOG_ID = 0;
    private static final int PAUSE_MENU_DIALOG_ID = 1;
    private static final int RESUME_MENU_DIALOG_ID = 2;

    @Override
    public void onDialogClick(String tag, int which) {

        if (tag.equals(TRANSFERS_DIALOG_ID)) {
            switch (which) {
                case CLEAR_MENU_DIALOG_ID:
                    //TransferManager.instance().clearComplete();
                    LOG.info("clear menu duialog");
                    break;
                case PAUSE_MENU_DIALOG_ID:
                    LOG.info("pause menu dialo");
                    break;
                case RESUME_MENU_DIALOG_ID:
                    LOG.info("resume menu dialog");
                    /*
                    boolean bittorrentDisconnected = false; //TransferManager.instance().isBittorrentDisconnected();
                    if (bittorrentDisconnected) {
                        UIUtils.showLongMessage(getActivity(), R.string.cant_resume_torrent_transfers);
                    } else {
                        if (NetworkManager.instance().isDataUp()) {
                            TransferManager.instance().resumeResumableTransfers();
                        } else {
                            UIUtils.showShortMessage(getActivity(), R.string.please_check_connection_status_before_resuming_download);
                        }
                    }
                    */
                    break;
            }
            setupAdapter();
        }
    }

    private void showContextMenu() {
        MenuDialog.MenuItem clear = new MenuDialog.MenuItem(CLEAR_MENU_DIALOG_ID, R.string.transfers_context_menu_clear_finished, R.drawable.contextmenu_icon_remove_transfer);
        MenuDialog.MenuItem pause = new MenuDialog.MenuItem(PAUSE_MENU_DIALOG_ID, R.string.transfers_context_menu_pause_stop_all_transfers, R.drawable.contextmenu_icon_pause_transfer);
        MenuDialog.MenuItem resume = new MenuDialog.MenuItem(RESUME_MENU_DIALOG_ID, R.string.transfers_context_resume_all_torrent_transfers, R.drawable.contextmenu_icon_play);

        List<MenuDialog.MenuItem> dlgActions = new ArrayList<>();
        TransferManager tm = TransferManager.instance();
        boolean stopped = Engine.instance().isStopped();
        final List<Transfer> transfers = tm.getTransfers();

        if (transfers != null && transfers.size() > 0) {



            if (someTransfersComplete(transfers)) {
                dlgActions.add(clear);
            }

            if (someTransfersActive(transfers)) {
                dlgActions.add(pause);
            }

            if (someTransfersInactive(transfers)) {
                dlgActions.add(resume);
            }
        }

        if (dlgActions.size() > 0) {
            MenuDialog dlg = MenuDialog.newInstance(TRANSFERS_DIALOG_ID, dlgActions);
            dlg.show(getFragmentManager());
        }
    }

    private boolean someTransfersInactive(List<Transfer> transfers) {
        for (Transfer t : transfers) {
            if (!t.isDownloading() && !t.isComplete()) return true;
        }

        return false;
    }

    private boolean someTransfersComplete(List<Transfer> transfers) {
        for (Transfer t : transfers) {
            if (t.isComplete()) {
                return true;
            }
        }

        return false;
    }

    private boolean someTransfersActive(List<Transfer> transfers) {
        for (Transfer t : transfers) {
            if (t.isDownloading()) return true;
        }
        return false;
    }

    private void startTransferFromURL() {
        String url = addTransferUrlTextView.getText();
        if (url != null && !url.isEmpty() && (url.startsWith("ed2k"))) {
            toggleAddTransferControls();
            if (url.startsWith("ed2k")) {
                if (Engine.instance().startDownload(url) != null) UIUtils.showLongMessage(getActivity(), R.string.torrent_url_added);
            }
            addTransferUrlTextView.setText("");
        } else {
            UIUtils.showLongMessage(getActivity(), R.string.please_enter_valid_url);
        }
    }

    public void startTransferFromLink(final String url) {
        if (url != null && !url.isEmpty() && (url.startsWith("ed2k"))) {
            if (url.startsWith("ed2k")) {
                if (Engine.instance().isStarted()) {
                    if (Engine.instance().startDownload(url) != null)
                        UIUtils.showLongMessage(getActivity(), R.string.torrent_url_added);
                } else {
                    UIUtils.showInformationDialog(getActivity()
                            , R.string.add_transfer_session_stopped_body
                            , R.string.add_transfer_session_stopped_title
                            ,false
                            , null);
                }
            }
        } else {
            UIUtils.showLongMessage(getActivity(), R.string.please_enter_valid_url);
        }
    }

    private void autoPasteMagnetOrURL() {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData primaryClip = clipboard.getPrimaryClip();
        if (primaryClip != null) {
            ClipData.Item itemAt = primaryClip.getItemAt(0);
            try {
                CharSequence charSequence = itemAt.getText();

                if (charSequence != null) {
                    String text;

                    if (charSequence instanceof String) {
                        text = (String) charSequence;
                    } else {
                        text = charSequence.toString();
                    }

                    if (text != null && !text.isEmpty()) {
                        if (text.startsWith("ed2k")) {
                            addTransferUrlTextView.setText(text.trim());
                            if (Engine.instance().startDownload(text) != null) {
                                UIUtils.showLongMessage(getActivity(), R.string.magnet_url_added);
                            }
                            clipboard.setPrimaryClip(ClipData.newPlainText("", ""));
                            toggleAddTransferControls();
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    private void toggleAddTransferControls() {
        if (addTransferUrlTextView.getVisibility() == View.GONE) {
            addTransferUrlTextView.setVisibility(View.VISIBLE);
            autoPasteMagnetOrURL();
            showAddTransfersKeyboard();
        } else {
            addTransferUrlTextView.setVisibility(View.GONE);
            addTransferUrlTextView.setText("");
            hideAddTransfersKeyboard();
        }
    }

    private void showAddTransfersKeyboard() {
        if (addTransferUrlTextView.getVisibility() == View.VISIBLE && (addTransferUrlTextView.getText().startsWith("http") || addTransferUrlTextView.getText().isEmpty())) {
            UIUtils.showKeyboard(addTransferUrlTextView.getAutoCompleteTextView().getContext(), addTransferUrlTextView.getAutoCompleteTextView());
        }
    }

    private void hideAddTransfersKeyboard() {
        InputMethodManager imm = (InputMethodManager) addTransferUrlTextView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(addTransferUrlTextView.getWindowToken(), 0);
    }


    private static File getBiggestSDCardDir(Context context) {
        try {
            final File externalFilesDir = context.getExternalFilesDir(null);
            // this occurs on the android emulator
            if (externalFilesDir == null) {
                return null;
            }
            String primaryPath = externalFilesDir.getParent();

            long biggestBytesAvailable = -1;

            File result = null;

            for (File f : SystemUtils.getExternalFilesDirs(context)) {
                if (!f.getAbsolutePath().startsWith(primaryPath)) {
                    long bytesAvailable = SystemUtils.getAvailableStorageSize(f);
                    if (bytesAvailable > biggestBytesAvailable) {
                        biggestBytesAvailable = bytesAvailable;
                        result = f;
                    }
                }
            }
            //System.out.println("FW.SystemUtils.getSDCardDir() -> " + result.getAbsolutePath());
            // -> /storage/extSdCard/Android/data/com.frostwire.android/files
            return result;
        } catch (Exception e) {
            // the context could be null due to a UI bad logic or context.getExternalFilesDir(null) could be null
            LOG.error("Error getting the biggest SD card", e);
        }

        return null;
    }

    private static final class TransferComparator implements Comparator<Transfer> {
        public int compare(Transfer lhs, Transfer rhs) {
            try {
                return -lhs.getCreated().compareTo(rhs.getCreated());
            } catch (Exception e) {
                // ignore, not really super important
            }
            return 0;
        }
    }

    public enum TransferStatus {
        ALL, DOWNLOADING, COMPLETED;

        private static TransferStatus[] STATUS_ARRAY = new TransferStatus[]{
                ALL,
                DOWNLOADING,
                COMPLETED};

        public static TransferStatus[] getAllStatusesArray() {
            return STATUS_ARRAY;
        }
    }


    private static final class ButtonAddTransferListener extends ClickAdapter<TransfersFragment> {

        ButtonAddTransferListener(TransfersFragment f) {
            super(f);
        }

        @Override
        public void onClick(TransfersFragment f, View v) {
            f.toggleAddTransferControls();
        }
    }

    private static final class ButtonMenuListener extends ClickAdapter<TransfersFragment> {

        ButtonMenuListener(TransfersFragment f) {
            super(f);
        }

        @Override
        public void onClick(TransfersFragment f, View v) {
            // temporary do nothing
            //f.showContextMenu();
        }
    }

    private static final class AddTransferTextListener extends ClickAdapter<TransfersFragment> implements AdapterView.OnItemClickListener, ClearableEditTextView.OnActionListener {

        AddTransferTextListener(TransfersFragment owner) {
            super(owner);
        }

        @Override
        public boolean onKey(TransfersFragment owner, View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                owner.startTransferFromURL();
                return true;
            }
            return false;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (Ref.alive(ownerRef)) {
                TransfersFragment owner = ownerRef.get();
                owner.startTransferFromURL();
            }
        }

        @Override
        public void onClear(View v) {
            if (Ref.alive(ownerRef)) {
                //TransfersFragment owner = ownerRef.get();
                //might clear.
                LOG.debug("onClear");
            }
        }

        @Override
        public void onTextChanged(View v, String str) {
        }
    }

    private static final class ButtonTabListener extends ClickAdapter<TransfersFragment> {

        private final TransferStatus status;

        ButtonTabListener(TransfersFragment f, TransferStatus status) {
            super(f);
            this.status = status;
        }

        @Override
        public void onClick(TransfersFragment f, View v) {
            f.selectedStatus = status;
            f.onTime();
        }
    }

    private static final class SDCardNotificationListener extends ClickAdapter<TransfersFragment> {

        SDCardNotificationListener(TransfersFragment owner) {
            super(owner);
        }

        @Override
        public void onClick(TransfersFragment owner, View v) {
            Intent i = new Intent(owner.getActivity(), SettingsActivity.class);
            i.setAction(Constants.ACTION_SETTINGS_SELECT_STORAGE);
            owner.getActivity().startActivity(i);
        }
    }

}
