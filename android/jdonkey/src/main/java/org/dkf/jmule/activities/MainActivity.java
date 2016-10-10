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

import android.app.*;
import android.content.*;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.SimpleDrawerListener;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import org.apache.commons.io.IOUtils;
import org.dkf.jed2k.android.ED2KService;
import org.dkf.jed2k.util.Ref;
import org.dkf.jmule.Engine;
import org.dkf.jmule.R;
import org.dkf.jmule.StoragePicker;
import org.dkf.jmule.activities.internal.MainController;
import org.dkf.jmule.activities.internal.MainMenuAdapter;
import org.dkf.jmule.core.AndroidPlatform;
import org.dkf.jmule.core.ConfigurationManager;
import org.dkf.jmule.core.Constants;
import org.dkf.jmule.core.Platforms;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * @author gubatron
 * @author aldenml
 */
public class MainActivity extends AbstractActivity implements
        AbstractDialog.OnDialogClickListener,
        ServiceConnection,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final Logger log = LoggerFactory.getLogger(MainActivity.class);
    private static final String FRAGMENTS_STACK_KEY = "fragments_stack";
    private static final String CURRENT_FRAGMENT_KEY = "current_fragment";
    private static final String LAST_BACK_DIALOG_ID = "last_back_dialog";
    private static final String SHUTDOWN_DIALOG_ID = "shutdown_dialog";
    private static boolean firstTime = true;
    private final Map<Integer, DangerousPermissionsChecker> permissionsCheckers;
    private MainController controller;
    private DrawerLayout drawerLayout;

    private ActionBarDrawerToggle drawerToggle;
    private View leftDrawer;
    private ListView listMenu;
    private SearchFragment search;
    private ServersFragment servers;
    private TransfersFragment transfers;
    private Fragment currentFragment;
    private final Stack<Integer> fragmentsStack;
    private BroadcastReceiver mainBroadcastReceiver;
    private boolean externalStoragePermissionsRequested = false;
    private InterstitialAd mInterstitialAd;
    private boolean applicationExit = false;

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
            return super.onKeyDown(keyCode, event);
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

        syncSlideMenu();
        updateHeader(getCurrentFragment());
    }

    public void onConfigurationUpdate() {
        setupMenuItems();
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
        initDrawerListener();
        leftDrawer = findView(R.id.activity_main_left_drawer);
        listMenu = findView(R.id.left_drawer);
        initAdMenuItemListener();
        setupFragments();
        setupMenuItems();
        setupInitialFragment(savedInstanceState);
        //playerSubscription = TimerService.subscribe(((PlayerNotifierView) findView(R.id.activity_main_player_notifier)).getRefresher(), 1);
        onNewIntent(getIntent());
        //SoftwareUpdater.instance().addConfigurationUpdateListener(this);
        setupActionBar();
        setupDrawer();
    }

    private void initAdMenuItemListener() {
        /*menuRemoveAdsItem = findView(R.id.slidermenu_ad_menuitem);
        RelativeLayout menuAd = findView(R.id.view_ad_menu_item_ad);
        menuAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BuyActivity.class);
                startActivity(intent);
            }
        });
        */
    }

    private void initDrawerListener() {
        drawerLayout = findView(R.id.drawer_layout);
        drawerLayout.setDrawerListener(new SimpleDrawerListener() {
            @Override
            public void onDrawerStateChanged(int newState) {
                refreshPlayerItem();
                refreshMenuRemoveAdsItem();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                syncSlideMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        if (isShutdown(intent)) {
            return;
        }

        String action = intent.getAction();

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

    private void openTorrentUrl(Intent intent) {
        try {
            //Open a Torrent from a URL or from a local file :), say from Astro File Manager.

            //Show me the transfer tab
            Intent i = new Intent(this, MainActivity.class);
            i.setAction(ED2KService.ACTION_SHOW_TRANSFERS);
            i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(i);

            //go!
            final String uri = intent.getDataString();
            intent.setAction(null);
            if (uri != null) {
                if (uri.startsWith("file") ||
                        uri.startsWith("http") ||
                        uri.startsWith("https") ||
                        uri.startsWith("magnet")) {
                    //TransferManager.instance().downloadTorrent(uri, new HandpickedTorrentDownloadDialogOnFetch(this));
                } else if (uri.startsWith("content")) {
                    String newUri = saveViewContent(this, Uri.parse(uri), "content-intent.torrent");
                    if (newUri != null) {
                        //TransferManager.instance().downloadTorrent(newUri, new HandpickedTorrentDownloadDialogOnFetch(this));
                    }
                }
            } else {
                log.warn("MainActivity.onNewIntent(): Couldn't start torrent download from Intent's URI, intent.getDataString() -> null");
                log.warn("(maybe URI is coming in another property of the intent object - #fragmentation)");
            }
        } catch (Throwable e) {
            log.error("Error opening torrent from intent", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        initDrawerListener();
        setupDrawer();
        initAdMenuItemListener();

        refreshPlayerItem();
        refreshMenuRemoveAdsItem();

        if (ConfigurationManager.instance().getBoolean(Constants.PREF_KEY_GUI_INITIAL_SETTINGS_COMPLETE)) {
            mainResume();
            //Offers.initAdNetworks(this);
        } else if (!isShutdown()){
            //controller.startWizardActivity();
        }

        checkLastSeenVersion();
        registerMainBroadcastReceiver();
        syncSlideMenu();

        //uncomment to test social links dialog
        //UIUtils.showSocialLinksDialog(this, true, null, "");

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

    private Map<Integer, DangerousPermissionsChecker> initPermissionsCheckers() {
        Map<Integer, DangerousPermissionsChecker> checkers = new HashMap<>();

        // EXTERNAL STORAGE ACCESS CHECKER.
        final DangerousPermissionsChecker externalStorageChecker =
                new DangerousPermissionsChecker(this, DangerousPermissionsChecker.EXTERNAL_STORAGE_PERMISSIONS_REQUEST_CODE);
        //externalStorageChecker.setPermissionsGrantedCallback(() -> {});
        checkers.put(DangerousPermissionsChecker.EXTERNAL_STORAGE_PERMISSIONS_REQUEST_CODE, externalStorageChecker);

        // WRITE SETTINGS (Setting the default ringtone requires this)
        final DangerousPermissionsChecker writeSettingsChecker =
                new DangerousPermissionsChecker(this, DangerousPermissionsChecker.WRITE_SETTINGS_PERMISSIONS_REQUEST_CODE);
        checkers.put(DangerousPermissionsChecker.WRITE_SETTINGS_PERMISSIONS_REQUEST_CODE, writeSettingsChecker);
        // the permissionGrantedCallBack will be set by whoever uses this during runtime.

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

        //if (!ConfigurationManager.instance().getBoolean(Constants.PREF_KEY_GUI_TOS_ACCEPTED)) {
            // we are still in the wizard.
        //    return;
        //}

        if (isShutdown()) {
            log.info("onCreate is shutdown, exit");
            return;
        }

        log.info("onCreate initialize Ad");

        checkExternalStoragePermissionsOrBindMusicService();
        MobileAds.initialize(getApplicationContext(), getResources().getString(R.string.banner_ad_1_id));

        applicationExit = false;
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.interstitioal_ad_1_id));

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                if (applicationExit) shutdown();
                else requestNewInterstitial();
            }

        });

        requestNewInterstitial();
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("6613A0A1A0D4EE0FABD0193C3A450CF6")
                .build();
        mInterstitialAd.loadAd(adRequest);
    }

    public boolean showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
            return true;
        }

        return false;
    }

    private void checkExternalStoragePermissionsOrBindMusicService() {
        DangerousPermissionsChecker checker = permissionsCheckers.get(DangerousPermissionsChecker.EXTERNAL_STORAGE_PERMISSIONS_REQUEST_CODE);
        if (!externalStoragePermissionsRequested && checker != null && checker.noAccess()) {
            checker.requestPermissions();
            externalStoragePermissionsRequested = true;
        }// else if (mToken == null && checker != null && !checker.noAccess()) {
         //   mToken = MusicUtils.bindToService(this, this);
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

        syncSlideMenu();
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
            File parent = data.getParentFile();

            if (!AndroidPlatform.saf(parent)) {
                return;
            }
            if (!Platforms.fileSystem().canWrite(parent) &&
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

    private void checkLastSeenVersion() {
        /*
        final String lastSeenVersion = ConfigurationManager.instance().getString(Constants.PREF_KEY_CORE_LAST_SEEN_VERSION);
        if (lastSeenVersion != null && !lastSeenVersion.isEmpty()) {
            //fresh install
            ConfigurationManager.instance().setString(Constants.PREF_KEY_CORE_LAST_SEEN_VERSION, Constants.FROSTWIRE_VERSION_STRING);
        } else if (!Constants.FROSTWIRE_VERSION_STRING.equals(lastSeenVersion)) {
            //just updated.
            ConfigurationManager.instance().setString(Constants.PREF_KEY_CORE_LAST_SEEN_VERSION, Constants.FROSTWIRE_VERSION_STRING);
        }
        */
    }

    private void toggleDrawer() {
        if (drawerLayout.isDrawerOpen(leftDrawer)) {
            drawerLayout.closeDrawer(leftDrawer);
        } else {
            drawerLayout.openDrawer(leftDrawer);
            syncSlideMenu();
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

    private void showShutdownDialog() {
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
        applicationExit = true;
        if (!showInterstitial()) finish();
    }

    private void onShutdownDialogButtonPositive() {
        applicationExit = true;
        Engine.instance().shutdown();
        if (!showInterstitial()) finish();
    }

    private void syncSlideMenu() {
        listMenu.clearChoices();
        invalidateOptionsMenu();

        Fragment fragment = getCurrentFragment();
        int menuId = R.id.menu_main_search;
        if (fragment instanceof ServersFragment) {
            menuId = R.id.menu_main_servers;
        }
        if (fragment instanceof SearchFragment) {
            menuId = R.id.menu_main_search;
        //} else if (fragment instanceof BrowsePeerFragment) {
        //    menuId = R.id.menu_main_library;
        } else if (fragment instanceof TransfersFragment) {
            menuId = R.id.menu_main_transfers;
        }

        setCheckedItem(menuId);
        updateHeader(getCurrentFragment());
    }

    private void setCheckedItem(int id) {
        try {
            listMenu.clearChoices();
            ((MainMenuAdapter) listMenu.getAdapter()).notifyDataSetChanged();

            int position = 0;
            MainMenuAdapter adapter = (MainMenuAdapter) listMenu.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                listMenu.setItemChecked(i, false);
                if (adapter.getItemId(i) == id) {
                    position = i;
                    break;
                }
            }

            if (id != -1) {
                listMenu.setItemChecked(position, true);
            }

            invalidateOptionsMenu();

            if (drawerToggle != null) {
                drawerToggle.syncState();
            }
        } catch (Exception e) { // protecting from weird android UI engine issues
            log.warn("Error setting slide menu item selected", e);
        }
    }

    private void refreshPlayerItem() {
        //if (playerItem != null) {
        //    playerItem.refresh();
        //}
    }

    private void refreshMenuRemoveAdsItem() {
        // only visible for basic or debug build
        /*int visibility = View.GONE;
        if (Constants.IS_GOOGLE_PLAY_DISTRIBUTION || Constants.IS_BASIC_AND_DEBUG) {
            // if they haven't paid for ads
            if (!Products.disabledAds(PlayStore.getInstance()) &&
                (playerItem == null || playerItem.getVisibility() == View.GONE)) {
                visibility = View.VISIBLE;
            }
        }
        menuRemoveAdsItem.setVisibility(visibility);
        */
    }

    private void setupMenuItems() {
        listMenu.setAdapter(new MainMenuAdapter(this));
        listMenu.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        listMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //onItemClick(AdapterView<?> parent, View view, int position, long id)
                syncSlideMenu();
                controller.closeSlideMenu();
                try {
                    if (id == R.id.menu_main_settings) {
                        controller.showPreferences();
                    } else if (id == R.id.menu_main_shutdown) {
                        showShutdownDialog();
                    } else {
                        listMenu.setItemChecked(position, true);
                        controller.switchFragment((int) id);
                    }
                } catch (Exception e) { // protecting from weird android UI engine issues
                    log.error("Error clicking slide menu item", e);
                }
            }
        });
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
            RelativeLayout placeholder = (RelativeLayout) getActionBar().getCustomView();
            if (placeholder != null && placeholder.getChildCount() > 0) {
                placeholder.removeAllViews();
            }

            if (fragment instanceof MainFragment) {
                View header = ((MainFragment) fragment).getHeader(this);
                if (placeholder != null && header != null) {
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                    params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                    placeholder.addView(header, params);
                }
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

    public void closeSlideMenu() {
        drawerLayout.closeDrawer(leftDrawer);
    }

    public SearchFragment getSearchFragment() { return search; }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle != null) {
            try {
                drawerToggle.onOptionsItemSelected(item);
            } catch (Exception t) {
                // usually java.lang.IllegalArgumentException: No drawer view found with gravity LEFT
                return false;
            }
            return true;
        }

        if (item == null) {
            return false;
        }

        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
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
        drawerToggle = new MenuDrawerToggle(this, drawerLayout);
        drawerLayout.setDrawerListener(drawerToggle);
    }

    public void onServiceConnected(final ComponentName name, final IBinder service) {
        //mService = IApolloService.Stub.asInterface(service);
    }

    /**
     * {@inheritDoc}
     */
    public void onServiceDisconnected(final ComponentName name) {
       // mService = null;
    }

    //@Override commented override since we are in API 16, but it will in API 23
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        DangerousPermissionsChecker checker = permissionsCheckers.get(requestCode);
        if (checker != null) {
            checker.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        //Offers.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private static final class MenuDrawerToggle extends ActionBarDrawerToggle {
        private final WeakReference<MainActivity> activityRef;

        MenuDrawerToggle(MainActivity activity, DrawerLayout drawerLayout) {
            super(activity, drawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close);

            // aldenml: even if the parent class holds a strong reference, I decided to keep a weak one
            this.activityRef = Ref.weak(activity);
        }

        @Override
        public void onDrawerClosed(View view) {
            if (Ref.alive(activityRef)) {
                activityRef.get().invalidateOptionsMenu();
                activityRef.get().syncSlideMenu();
            }
        }

        @Override
        public void onDrawerOpened(View drawerView) {
            if (Ref.alive(activityRef)) {
                UIUtils.hideKeyboardFromActivity(activityRef.get());
                activityRef.get().invalidateOptionsMenu();
                activityRef.get().syncSlideMenu();
            }
        }

        @Override
        public void onDrawerStateChanged(int newState) {
            if (Ref.alive(activityRef)) {
                MainActivity activity = activityRef.get();
                activity.refreshPlayerItem();
                activity.refreshMenuRemoveAdsItem();
                activity.syncSlideMenu();
            }
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
}
