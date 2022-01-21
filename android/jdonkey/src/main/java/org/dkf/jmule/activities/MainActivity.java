/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2015, FrostWire(R). All rights reserved.
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

package org.dkf.jmule.activities;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;

import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.core.app.ActivityCompat;

import org.apache.commons.io.IOUtils;
import org.dkf.jed2k.EMuleLink;
import org.dkf.jmule.*;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.kad.KadNodesDat;
import org.dkf.jed2k.protocol.server.ServerMet;
import org.dkf.jed2k.util.Ref;
import org.dkf.jmule.Engine;
import org.dkf.jmule.R;
import org.dkf.jmule.StoragePicker;
import org.dkf.jmule.activities.internal.MainController;
import org.dkf.jmule.activities.internal.MainMenuAdapter;
import org.dkf.jmule.dialogs.HandpickedCollectionDownloadDialog;
import org.dkf.jmule.dialogs.SDPermissionDialog;
import org.dkf.jmule.dialogs.YesNoDialog;
import org.dkf.jmule.fragments.MainFragment;
import org.dkf.jmule.fragments.SearchFragment;
import org.dkf.jmule.fragments.ServersFragment;
import org.dkf.jmule.fragments.TransfersFragment;
import org.dkf.jmule.fragments.TransfersFragment.TransferStatus;
import org.dkf.jmule.util.DangerousPermissionsChecker;
import org.dkf.jmule.util.UIUtils;
import org.dkf.jmule.views.AbstractActivity;
import org.dkf.jmule.views.AbstractDialog;
import org.dkf.jmule.views.preference.StoragePreference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

/**
 * @author gubatron
 * @author aldenml
 */
public class MainActivity extends AbstractActivity implements
        AbstractDialog.OnDialogClickListener,
        DialogInterface.OnClickListener,
        ServiceConnection,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final Logger log = LoggerFactory.getLogger(MainActivity.class);
    private static final String FRAGMENTS_STACK_KEY = "fragments_stack";
    private static final String CURRENT_FRAGMENT_KEY = "current_fragment";
    private static final String LAST_BACK_DIALOG_ID = "last_back_dialog";
    private static final String SHUTDOWN_DIALOG_ID = "shutdown_dialog";
    private static boolean firstTime = true;
    private final SparseArray<DangerousPermissionsChecker> permissionsCheckers;
    private MainController controller;
    private NavigationMenu navigationMenu;

    private SearchFragment search;
    private ServersFragment servers;
    private TransfersFragment transfers;
    private Fragment currentFragment;
    private final Stack<Integer> fragmentsStack;
    private BroadcastReceiver mainBroadcastReceiver;
    private boolean externalStoragePermissionsRequested = false;
    private ServerMet lastLoadedServers = null;

    public MainActivity() {
        super(R.layout.activity_main);
        this.controller = new MainController(this);
        this.fragmentsStack = new Stack<>();
        this.permissionsCheckers = initPermissionsCheckers();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SEARCH) {
            if (!(getCurrentFragment() instanceof SearchFragment)) {
                controller.switchFragment(R.id.menu_main_search);
            }
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            toggleDrawer();
        } else {
            try {
                return super.onKeyDown(keyCode, event);
            } catch (NullPointerException e) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        if (fragmentsStack.size() > 1) {
            try {
                fragmentsStack.pop();
                int id = fragmentsStack.peek();
                Fragment fragment = getFragmentManager().findFragmentById(id);
                switchContent(fragment, false);
            } catch (Exception e) {
                // don't break the app
                showLastBackDialog();
            }
        } else {
            showLastBackDialog();
        }

        updateHeader(getCurrentFragment());
    }

    public void shutdown() {
        finish();
        Engine.instance().shutdown();
    }

    private boolean isShutdown() {
        return isShutdown(null);
    }

    private boolean isShutdown(Intent intent) {
        if (intent == null) {
            intent = getIntent();
        }
        boolean result = intent != null && intent.getBooleanExtra("shutdown-" + ConfigurationManager.instance().getUUIDString(), false);
        if (result) {
            shutdown();
        }
        return result;
    }

    @Override
    protected void initComponents(Bundle savedInstanceState) {
        if (isShutdown()) {
            return;
        }
        updateNavigationMenu();
        setupFragments();
        setupInitialFragment(savedInstanceState);
        onNewIntent(getIntent());
        setupActionBar();
        setupDrawer();
    }

    private static List<EMuleLink> parseCollectionContent(Context context, Uri uri) {
        InputStream inStream = null;
        List<EMuleLink> res = new LinkedList<>();

        try {
            inStream = context.getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
            String str;
            while ((str = reader.readLine()) != null) {
                log.info("read line from content {}", str);
                try {
                    res.add(EMuleLink.fromString(str));
                } catch(JED2KException e) {
                    log.warn("unable to parse line in collection {}, skip", e);
                }
            }
        } catch (Exception e) {
            log.error("error when reading file {} {}", uri, e);
        } finally {
            IOUtils.closeQuietly(inStream);
        }

        return res;
    }

    public void updateNavigationMenu(boolean updateAvailable) {
        log.info("updateNavigationMenu(" + updateAvailable + ")");
        if (navigationMenu == null) {
            setupDrawer();
        }
        if (updateAvailable) {
            // make sure it will remember this, even if the menu gets destroyed
            getIntent().putExtra("updateAvailable", true);
            navigationMenu.onUpdateAvailable();
        }
    }

    private void updateNavigationMenu() {
        Intent intent = getIntent();
        if (intent != null) {
            updateNavigationMenu(intent.getBooleanExtra("updateAvailable", false));
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        log.info("[main activity] on new intent {}", intent);

        if (intent == null) {
            return;
        }

        if (isShutdown(intent)) {
            return;
        }

        String action = intent.getAction();

        // view action here - check type of link and load data
        if (action != null && action.equals("android.intent.action.VIEW")) {
            try {
                final String uri = intent.getDataString();
                if (uri != null && uri.startsWith("content")) {
                    List<EMuleLink> links = parseCollectionContent(this, Uri.parse(uri));
                    log.info("link size {}", links.size());

                    if (!Engine.instance().isStarted()) {
                        UIUtils.showInformationDialog(this
                                , R.string.add_collection_session_stopped_body
                                , R.string.add_collection_session_stopped_title
                                ,false
                                , null);
                        return;
                    }

                    if (!links.isEmpty()) {
                        controller.showTransfers(TransferStatus.ALL);
                        HandpickedCollectionDownloadDialog dialog = HandpickedCollectionDownloadDialog.newInstance(this, links);
                        dialog.show(getFragmentManager());
                    } else {
                        UIUtils.showInformationDialog(this
                                , R.string.add_collection_empty
                                , R.string.add_collection_session_stopped_title
                                ,false
                                , null);
                    }
                    return;
                }

                EMuleLink link = EMuleLink.fromString(uri);

                if (link.getType().equals(EMuleLink.LinkType.SERVER)) {
                    ServerMet sm = new ServerMet();
                    ConfigurationManager.instance().getSerializable(Constants.PREF_KEY_SERVERS_LIST, sm);

                    try {
                        sm.addServer(ServerMet.ServerMetEntry.create(link.getStringValue()
                                , (int)link.getNumberValue()
                                , "[" + link.getStringValue() + "]"
                                , ""));

                        ConfigurationManager.instance().setSerializable(Constants.PREF_KEY_SERVERS_LIST, sm);
                        servers.setupAdapter();
                        controller.showServers();
                    } catch (JED2KException e) {
                        e.printStackTrace();
                    }
                }
                else if (link.getType().equals(EMuleLink.LinkType.SERVERS)) {
                    final String serversLink = link.getStringValue();
                    final MainActivity main = this;
                    AsyncTask<Void, Void, ServerMet> task = new AsyncTask<Void, Void, ServerMet>() {

                        @Override
                        protected ServerMet doInBackground(Void... voids) {
                            try {
                                byte[] data = IOUtils.toByteArray(new URI(serversLink));
                                ByteBuffer buffer = ByteBuffer.wrap(data);
                                buffer.order(ByteOrder.LITTLE_ENDIAN);
                                ServerMet sm = new ServerMet();
                                sm.get(buffer);
                                return sm;
                            } catch(Exception e) {
                                log.error("unable to load servers {}", e);
                            }

                            return null;
                        }

                        @Override
                        protected void onPostExecute(ServerMet result) {
                            if (result != null) {
                                lastLoadedServers = result;
                                UIUtils.showYesNoDialog(main
                                        , R.string.add_servers_list_text
                                        , R.string.add_servers_list_title,
                                        main);
                            } else {
                                UIUtils.showInformationDialog(main, R.string.link_download_failed, R.string.link_download_failed, true, null);
                            }
                        }
                    };

                    task.execute();
                }
                else if (link.getType().equals(EMuleLink.LinkType.NODES)) {
                    final String serversLink = link.getStringValue();
                    final MainActivity main = this;
                    AsyncTask<Void, Void, KadNodesDat> task = new AsyncTask<Void, Void, KadNodesDat>() {

                        @Override
                        protected KadNodesDat doInBackground(Void... voids) {
                            try {
                                byte[] data = IOUtils.toByteArray(new URI(serversLink));
                                ByteBuffer buffer = ByteBuffer.wrap(data);
                                buffer.order(ByteOrder.LITTLE_ENDIAN);
                                KadNodesDat sm = new KadNodesDat();
                                sm.get(buffer);
                                return sm;
                            } catch(Exception e) {
                                log.error("unable to load nodes dat {}", e);
                            }

                            return null;
                        }

                        @Override
                        protected void onPostExecute(KadNodesDat result) {
                            if (result != null) {
                                if (!Engine.instance().addDhtNodes(result)) {
                                    UIUtils.showInformationDialog(main, R.string.nodes_link_open_error_text, R.string.nodes_link_open_error_title, false, null);
                                }
                            } else {
                                UIUtils.showInformationDialog(main, R.string.link_download_failed, R.string.link_download_failed, true, null);
                            }
                        }
                    };

                    task.execute();
                }
                else if (link.getType().equals(EMuleLink.LinkType.FILE)) {
                    transfers.startTransferFromLink(intent.getDataString());
                    controller.showTransfers(TransferStatus.ALL);
                }
                else {
                    log.error("wtf? link unrecognized {}", intent.getDataString());
                }

            } catch(JED2KException e) {
                log.error("intent get data parse error {}", e.toString());
                UIUtils.showInformationDialog(this, R.string.intent_link_parse_error, R.string.add_servers_list_title, true, null);
            }
        }

        if (action != null) {
            if (action.equals(ED2KService.ACTION_SHOW_TRANSFERS)) {
                intent.setAction(null);
                controller.showTransfers(TransferStatus.ALL);
            } else if (action.equals(ED2KService.ACTION_REQUEST_SHUTDOWN)) {
                showShutdownDialog();
            }
        }

        if (intent.hasExtra(ED2KService.EXTRA_DOWNLOAD_COMPLETE_NOTIFICATION)) {
            controller.showTransfers(TransferStatus.COMPLETED);

            try {
                ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(Constants.NOTIFICATION_DOWNLOAD_TRANSFER_FINISHED);
                Bundle extras = intent.getExtras();
                if (extras.containsKey(Constants.EXTRA_DOWNLOAD_COMPLETE_PATH)) {
                    File file = new File(extras.getString(Constants.EXTRA_DOWNLOAD_COMPLETE_PATH));
                    if (file.isFile()) {
                        //UIUtils.openFile(this, file.getAbsoluteFile());
                    }
                }
            } catch (Exception e) {
                log.warn("Error handling download complete notification", e);
            }
        }

        if (intent.hasExtra(Constants.EXTRA_FINISH_MAIN_ACTIVITY)) {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupDrawer();
        syncNavigationMenu();
        updateNavigationMenu();

        if (ConfigurationManager.instance().getBoolean(Constants.PREF_KEY_GUI_INITIAL_SETTINGS_COMPLETE)) {
            mainResume();
        } else if (!isShutdown()){
            controller.startWizardActivity();
        }

        registerMainBroadcastReceiver();

        if (ConfigurationManager.instance().getBoolean(Constants.PREF_KEY_GUI_TOS_ACCEPTED)) {
            checkExternalStoragePermissionsOrBindMusicService();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mainBroadcastReceiver != null) {
            try {
                unregisterReceiver(mainBroadcastReceiver);
            } catch (Exception ignored) {
                //oh well (the api doesn't provide a way to know if it's been registered before,
                //seems like overkill keeping track of these ourselves.)
            }
        }
    }

    private SparseArray<DangerousPermissionsChecker> initPermissionsCheckers() {
        SparseArray<DangerousPermissionsChecker> checkers = new SparseArray<>();
        // EXTERNAL STORAGE ACCESS CHECKER.
        final DangerousPermissionsChecker externalStorageChecker =
                new DangerousPermissionsChecker(this, DangerousPermissionsChecker.EXTERNAL_STORAGE_PERMISSIONS_REQUEST_CODE);
        //externalStorageChecker.setPermissionsGrantedCallback(() -> {});
        checkers.put(DangerousPermissionsChecker.EXTERNAL_STORAGE_PERMISSIONS_REQUEST_CODE, externalStorageChecker);
        // COARSE
        final DangerousPermissionsChecker accessCoarseLocationChecker =
                new DangerousPermissionsChecker(this, DangerousPermissionsChecker.ACCESS_COARSE_LOCATION_PERMISSIONS_REQUEST_CODE);
        checkers.put(DangerousPermissionsChecker.ACCESS_COARSE_LOCATION_PERMISSIONS_REQUEST_CODE, accessCoarseLocationChecker);
        // add more permissions checkers if needed...
        return checkers;
    }

    private void registerMainBroadcastReceiver() {
        mainBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                if (Constants.ACTION_NOTIFY_SDCARD_MOUNTED.equals(intent.getAction())) {
                    onNotifySdCardMounted();
                }
            }
        };

        IntentFilter bf = new IntentFilter(Constants.ACTION_NOTIFY_SDCARD_MOUNTED);
        registerReceiver(mainBroadcastReceiver, bf);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (outState != null) {
            super.onSaveInstanceState(outState);
            saveLastFragment(outState);
            saveFragmentsStack(outState);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!ConfigurationManager.instance().getBoolean(Constants.PREF_KEY_GUI_TOS_ACCEPTED)) {
            // we are still in the wizard.
            return;
        }

        if (isShutdown()) {
            return;
        }

        checkExternalStoragePermissionsOrBindMusicService();
    }

    private void checkAccessCoarseLocationPermissions() {
        DangerousPermissionsChecker checker = permissionsCheckers.get(DangerousPermissionsChecker.ACCESS_COARSE_LOCATION_PERMISSIONS_REQUEST_CODE);
        if (checker != null && !checker.hasAskedBefore()) {
            checker.requestPermissions();
            ConfigurationManager.instance().setBoolean(Constants.ASKED_FOR_ACCESS_COARSE_LOCATION_PERMISSIONS, true);
        } else {
            log.info("Asked for ACCESS_COARSE_LOCATION before, skipping.");
        }
    }

    private void checkExternalStoragePermissions() {
        DangerousPermissionsChecker checker = permissionsCheckers.get(DangerousPermissionsChecker.EXTERNAL_STORAGE_PERMISSIONS_REQUEST_CODE);
        boolean shouldShowRequestPermissionRationaleForReadExternal = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (shouldShowRequestPermissionRationaleForReadExternal || (!externalStoragePermissionsRequested && checker != null && checker.noAccess())) {
            checker.requestPermissions();
            externalStoragePermissionsRequested = true;
        }
    }

    private void checkExternalStoragePermissionsOrBindMusicService() {
        DangerousPermissionsChecker checker = permissionsCheckers.get(DangerousPermissionsChecker.EXTERNAL_STORAGE_PERMISSIONS_REQUEST_CODE);
        if (!externalStoragePermissionsRequested && checker != null && checker.noAccess()) {
            checker.requestPermissions();
            externalStoragePermissionsRequested = true;
        }// else if (mToken == null && checker != null && !checker.noAccess()) {
        //    mToken = MusicUtils.bindToService(this, this);
        //}
    }

    private void onNotifySdCardMounted() {
        //transfers.initStorageRelatedRichNotifications(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void saveLastFragment(Bundle outState) {
        Fragment fragment = getCurrentFragment();
        if (fragment != null) {
            getFragmentManager().putFragment(outState, CURRENT_FRAGMENT_KEY, fragment);
        }
    }

    private void mainResume() {
        checkSDPermission();
        syncNavigationMenu();
        if (firstTime) {
            firstTime = false;
            Engine.instance().startServices(); // it's necessary for the first time after wizard
        }
    }

    private void checkSDPermission() {
        if (!AndroidPlatform.saf()) {
            return;
        }

        try {
            File data = Platforms.data();
            //File parent = data.getParentFile();

            if (!AndroidPlatform.saf(data)) {
                return;
            }

            log.info("check write permissions for {}", data);

            if (!Platforms.fileSystem().canWrite(data) &&
                    !SDPermissionDialog.visible) {
                SDPermissionDialog dlg = SDPermissionDialog.newInstance();
                dlg.show(getFragmentManager());
            }
        } catch (Exception e) {
            // we can't do anything about this
            log.error("Unable to detect if we have SD permissions", e);
        }
    }

    private void handleSDPermissionDialogClick(int which) {
        if (which == Dialog.BUTTON_POSITIVE) {
            StoragePicker.show(this);
        } else {
            // TODO:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == StoragePicker.SELECT_FOLDER_REQUEST_CODE) {
            StoragePreference.onDocumentTreeActivityResult(this, requestCode, resultCode, data);
        } else if (!DangerousPermissionsChecker.handleOnWriteSettingsActivityResult(this)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void toggleDrawer() {
        if (navigationMenu.isOpen()) {
            navigationMenu.hide();
        } else {
            navigationMenu.show();
            syncNavigationMenu();
        }
        updateHeader(getCurrentFragment());
    }

    private void showLastBackDialog() {
        YesNoDialog dlg = YesNoDialog.newInstance(
                LAST_BACK_DIALOG_ID,
                R.string.minimize_application,
                R.string.are_you_sure_you_wanna_leave,
                YesNoDialog.FLAG_DISMISS_ON_OK_BEFORE_PERFORM_DIALOG_CLICK);
        dlg.show(getFragmentManager()); //see onDialogClick
    }

    public void showShutdownDialog() {
        YesNoDialog dlg = YesNoDialog.newInstance(
                SHUTDOWN_DIALOG_ID,
                R.string.app_shutdown_dlg_title,
                R.string.app_shutdown_dlg_message,
                YesNoDialog.FLAG_DISMISS_ON_OK_BEFORE_PERFORM_DIALOG_CLICK);
        dlg.show(getFragmentManager()); //see onDialogClick
    }

    public void onDialogClick(String tag, int which) {
        if (tag.equals(LAST_BACK_DIALOG_ID) && which == Dialog.BUTTON_POSITIVE) {
            onLastDialogButtonPositive();
        } else if (tag.equals(SHUTDOWN_DIALOG_ID) && which == Dialog.BUTTON_POSITIVE) {
            onShutdownDialogButtonPositive();
        } else if (tag.equals(SDPermissionDialog.TAG)) {
            handleSDPermissionDialogClick(which);
        }
    }

    private void onLastDialogButtonPositive() {
        finish();
    }

    private void onShutdownDialogButtonPositive() {
        Engine.instance().shutdown();
        finish();
    }

    private void setCheckedItem(int id) {
        if (navigationMenu != null) {
            navigationMenu.updateCheckedItem(id);
        }
    }


    private void setupFragments() {
        servers = (ServersFragment) getFragmentManager().findFragmentById(R.id.activity_main_fragment_servers);
        search = (SearchFragment) getFragmentManager().findFragmentById(R.id.activity_main_fragment_search);
        transfers = (TransfersFragment) getFragmentManager().findFragmentById(R.id.activity_main_fragment_transfers);
        hideFragments(getFragmentManager().beginTransaction()).commit();
    }

    private FragmentTransaction hideFragments(FragmentTransaction ts) {
        return ts.hide(search).hide(transfers).hide(servers);
    }

    private void setupInitialFragment(Bundle savedInstanceState) {
        Fragment fragment = null;

        if (savedInstanceState != null) {
            fragment = getFragmentManager().getFragment(savedInstanceState, CURRENT_FRAGMENT_KEY);
            restoreFragmentsStack(savedInstanceState);
        }

        if (fragment == null) {
            fragment = servers;
            setCheckedItem(R.id.menu_main_servers);
        }

        switchContent(fragment);
    }

    private void saveFragmentsStack(Bundle outState) {
        int[] stack = new int[fragmentsStack.size()];
        for (int i = 0; i < stack.length; i++) {
            stack[i] = fragmentsStack.get(i);
        }
        outState.putIntArray(FRAGMENTS_STACK_KEY, stack);
    }

    private void restoreFragmentsStack(Bundle savedInstanceState) {
        try {
            int[] stack = savedInstanceState.getIntArray(FRAGMENTS_STACK_KEY);
            for (int id : stack) {
                fragmentsStack.push(id);
            }
        } catch (Exception ignored) {
        }
    }

    private void updateHeader(Fragment fragment) {
        try {
            Toolbar toolbar = findToolbar();
            if (toolbar == null) {
                log.warn("updateHeader(): Check your logic, no actionBar available");
                return;
            }
            if (fragment instanceof MainFragment) {
                View header = ((MainFragment) fragment).getHeader(this);
                if (header != null) {
                    setToolbarView(header);
                }
            }
            if (navigationMenu != null) {
                MenuItem item = navigationMenu.getCheckedItem();
                setTitle(item.getTitle());
            }
        } catch (Throwable e) {
            log.error("Error updating main header", e);
        }
    }

    private void switchContent(Fragment fragment, boolean addToStack) {
        hideFragments(getFragmentManager().beginTransaction()).show(fragment).commitAllowingStateLoss();
        if (addToStack && (fragmentsStack.isEmpty() || fragmentsStack.peek() != fragment.getId())) {
            fragmentsStack.push(fragment.getId());
        }
        currentFragment = fragment;
        updateHeader(fragment);

        if (currentFragment instanceof MainFragment) {
            ((MainFragment) currentFragment).onShow();
        }
    }

    /*
     * The following methods are only public to be able to use them from another package(internal).
     */

    public Fragment getFragmentByMenuId(int id) {
        switch (id) {
            case R.id.menu_main_servers:
                return servers;
            case R.id.menu_main_search:
                return search;
            case R.id.menu_main_transfers:
                return transfers;
            default:
                return null;
        }
    }

    public void switchContent(Fragment fragment) {
        switchContent(fragment, true);
    }

    public Fragment getCurrentFragment() {
        return currentFragment;
    }

    public SearchFragment getSearchFragment() { return search; }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (navigationMenu != null) {
            try {
                navigationMenu.onOptionsItemSelected(item);
            } catch (Throwable t) {
                // usually java.lang.IllegalArgumentException: No drawer view found with gravity LEFT
                return false;
            }
            return false;
        }
        if (item == null) {
            return false;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        navigationMenu.syncState();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        navigationMenu.syncState();
    }

    private void setupActionBar() {
        ActionBar bar = getActionBar();
        if (bar != null) {
            bar.setCustomView(R.layout.view_custom_actionbar);
            bar.setDisplayShowCustomEnabled(true);
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setHomeButtonEnabled(true);
        }
    }

    private void setupDrawer() {
        DrawerLayout drawerLayout = findView(R.id.drawer_layout);
        Toolbar toolbar = findToolbar();
        navigationMenu = new NavigationMenu(controller, drawerLayout, toolbar);
    }

    public void onServiceConnected(final ComponentName name, final IBinder service) { }
    public void onServiceDisconnected(final ComponentName name) { }

    //@Override commented override since we are in API 16, but it will in API 23
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        DangerousPermissionsChecker checker = permissionsCheckers.get(requestCode);
        if (checker != null) {
            checker.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        if (lastLoadedServers != null) {
            ConfigurationManager.instance().setSerializable(Constants.PREF_KEY_SERVERS_LIST, lastLoadedServers);
            servers.setupAdapter();
            controller.showServers();
        }
    }

    // TODO: refactor and move this method for a common place when needed
    private static String saveViewContent(Context context, Uri uri, String name) {
        InputStream inStream = null;
        OutputStream outStream = null;
        if (!Platforms.temp().exists()) {
            Platforms.temp().mkdirs();
        }
        File target = new File(Platforms.temp(), name);
        try {
            inStream = context.getContentResolver().openInputStream(uri);
            outStream = new FileOutputStream(target);

            byte[] buffer = new byte[16384]; // MAGIC_NUMBER
            int bytesRead;
            while ((bytesRead = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }

        } catch (Exception e) {
            log.error("Error when copying file from " + uri + " to temp/" + name, e);
            return null;
        } finally {
            IOUtils.closeQuietly(inStream);
            IOUtils.closeQuietly(outStream);
        }

        return "file://" + target.getAbsolutePath();
    }

    public Fragment getFragmentByNavMenuId(int id) {
        if (id == R.id.menu_main_search) {
            return search;
        } else if (id == R.id.menu_main_servers) {
            return servers;
        } else if (id == R.id.menu_main_transfers) {
            return transfers;
        }
        return null;
    }

    private int getNavMenuIdByFragment(Fragment fragment) {
        int menuId = -1;
        if (fragment == search) {
            menuId = R.id.menu_main_search;
        } else if (fragment == servers) {
            menuId = R.id.menu_main_servers;
        } else if (fragment == transfers) {
            menuId = R.id.menu_main_transfers;
        }
        return menuId;
    }

    public void syncNavigationMenu() {
        invalidateOptionsMenu();
        navigationMenu.updateCheckedItem(getNavMenuIdByFragment(getCurrentFragment()));
    }
}
