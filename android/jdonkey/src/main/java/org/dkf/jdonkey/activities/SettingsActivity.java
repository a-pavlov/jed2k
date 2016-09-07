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

package org.dkf.jdonkey.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.*;
import android.preference.Preference.OnPreferenceChangeListener;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import org.dkf.jdonkey.R;
import org.dkf.jdonkey.StoragePicker;
import org.dkf.jdonkey.core.AndroidPlatform;
import org.dkf.jdonkey.core.ConfigurationManager;
import org.dkf.jdonkey.core.Constants;
import org.dkf.jdonkey.util.UIUtils;
import org.dkf.jdonkey.views.preference.NumberPickerPreference;
import org.dkf.jdonkey.views.preference.SimpleActionPreference;
import org.dkf.jdonkey.views.preference.StoragePreference;
import org.dkf.jed2k.android.ED2KService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * See {@link ConfigurationManager}
 *
 * @author gubatron
 * @author aldenml
 * @author sssemil
 */
public class SettingsActivity extends PreferenceActivity {
    private static final Logger LOG = LoggerFactory.getLogger(SettingsActivity.class);
    private static final boolean INTERNAL_BUILD = true;
    private static String currentPreferenceKey = null;
    private boolean finishOnBack = false;
    private long removeAdsPurchaseTime = 0;

    @Override
    protected void onResume() {
        super.onResume();
        setupComponents();
        initializePreferenceScreen(getPreferenceScreen());
        if (currentPreferenceKey != null) {
            onPreferenceTreeClick(getPreferenceScreen(), getPreferenceManager().findPreference(currentPreferenceKey));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.application_preferences);

        hideActionBarIcon(getActionBar());

        setupComponents();

        String action = getIntent().getAction();
        if (action != null) {
            getIntent().setAction(null);
            if (action.equals(Constants.ACTION_SETTINGS_SELECT_STORAGE)) {
                StoragePreference.invokeStoragePreference(this);
            } else if (action.equals(Constants.ACTION_SETTINGS_OPEN_TORRENT_SETTINGS)) {
                finishOnBack = true;
                openPreference("jdonkey.prefs.transfer.preference_category");
                return;
            }
        }

        updateConnectSwitch();
    }

    private void hideActionBarIcon(ActionBar bar) {
        if (bar != null) {
            LOG.info("set home button enabled");
            bar.setHomeButtonEnabled(true);
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setDisplayShowHomeEnabled(false);
            bar.setDisplayShowTitleEnabled(true);
            bar.setIcon(android.R.color.transparent);
        }
    }

    private void setupComponents() {
        setupConnectSwitch();
        setupStorageOption();
        setupOtherOptions();
        setupTransferOptions();
        setupClearIndex();
        setupStore(removeAdsPurchaseTime);
    }

    private void setupTransferOptions() {
        setupMaxDownloads();
        setupMaxTotalConnections();
    }

    private void setupMaxTotalConnections() {
        NumberPickerPreference pickerPreference = (NumberPickerPreference) findPreference(Constants.PREF_KEY_TRANSFER_MAX_TOTAL_CONNECTIONS);
        if (pickerPreference != null) {
            pickerPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    LOG.info("explicit setup total connections to {}", (int)newValue);
                    return true;
                }
            });
        }
    }

    private void setupMaxDownloads() {
        NumberPickerPreference pickerPref = (NumberPickerPreference) findPreference(Constants.PREF_KEY_TRANSFER_MAX_DOWNLOADS);
        if (pickerPref != null) {
            pickerPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    LOG.info("explicit set max downloads {}", (int)newValue);
                    return true;
                }
            });
        }
    }

    private void setupOtherOptions() {
        setupPermanentStatusNotificationOption();
    }

    private void setupPermanentStatusNotificationOption() {
        final CheckBoxPreference enablePermanentStatusNotification = (CheckBoxPreference) findPreference(Constants.PREF_KEY_GUI_ENABLE_PERMANENT_STATUS_NOTIFICATION);
        if (enablePermanentStatusNotification != null) {
            enablePermanentStatusNotification.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    final boolean notificationEnabled = (boolean) newValue;
                    if (!notificationEnabled) {
                        NotificationManager notificationService = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        if (notificationService != null) {
                            notificationService.cancel(ED2KService.ED2K_STATUS_NOTIFICATION);
                        }
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LOG.info("options item selected {}", item);
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupClearIndex() {
        final SimpleActionPreference preference = (SimpleActionPreference) findPreference("jdonkey.prefs.internal.clear_index");

        if (preference != null) {
            updateIndexSummary(preference);
            preference.setOnActionListener(new OnClickListener() {
                public void onClick(View v) {
                    //LocalSearchEngine.instance().clearCache();
                    UIUtils.showShortMessage(SettingsActivity.this, R.string.deleted_crawl_cache);
                    updateIndexSummary(preference);
                }
            });
        }
    }

    private void updateIndexSummary(SimpleActionPreference preference) {
        //float size = (((float) LocalSearchEngine.instance().getCacheSize()) / 1024) / 1024;
        //preference.setSummary(getString(R.string.crawl_cache_size, size));
    }

    private void updateConnectSwitch() {
        SwitchPreference preference = (SwitchPreference) findPreference("jdonkey.prefs.internal.connect_disconnect");
        if (preference != null) {
            final OnPreferenceChangeListener onPreferenceChangeListener = preference.getOnPreferenceChangeListener();
            preference.setOnPreferenceChangeListener(null);

            preference.setSummary(R.string.ed2k_network_summary);
            preference.setEnabled(true);
            /*
            if (Engine.instance().isStarted()) {
                preference.setChecked(true);
            } else if (Engine.instance().isStarting() || Engine.instance().isStopping()) {
                connectSwitchImOnIt(preference);
            } else if (Engine.instance().isStopped() || Engine.instance().isDisconnected()) {
                preference.setChecked(false);
            }
            */
            preference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        }
    }

    private void connectSwitchImOnIt(SwitchPreference preference) {
        final OnPreferenceChangeListener onPreferenceChangeListener = preference.getOnPreferenceChangeListener();
        preference.setOnPreferenceChangeListener(null);
        preference.setEnabled(false);
        preference.setSummary(R.string.im_on_it);
        preference.setOnPreferenceChangeListener(onPreferenceChangeListener);
    }

    private void setupConnectSwitch() {
        SwitchPreference preference = (SwitchPreference) findPreference("jdonkey.prefs.internal.connect_disconnect");
        if (preference != null) {
            preference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    final boolean newStatus = ((Boolean) newValue).booleanValue();
                    //if (Engine.instance().isStarted() && !newStatus) {
                    //    disconnect();
                    //} else if (newStatus && (Engine.instance().isStopped() || Engine.instance().isDisconnected())) {
                    //    connect();
                    //}
                    return true;
                }
            });
        }
    }

    private void setupStorageOption() {
        // intentional repetition of preference value here
        String kitkatKey = "jdonkey.prefs.storage.path";
        String lollipopKey = "jdonkey.prefs.storage.path_asf";

        PreferenceCategory category = (PreferenceCategory) findPreference("jdonkey.prefs.general");

        if (AndroidPlatform.saf()) {
            // make sure this won't be saved for kitkat
            Preference p = findPreference(kitkatKey);
            if (p != null) {
                category.removePreference(p);
            }
            p = findPreference(lollipopKey);
            if (p != null) {
                p.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        StoragePreference.updateStorageOptionSummary(SettingsActivity.this, newValue.toString());
                        return true;
                    }
                });
                StoragePreference.updateStorageOptionSummary(this, ConfigurationManager.instance().getStoragePath());
            }
        } else {
            Preference p = findPreference(lollipopKey);
            if (p != null) {
                category.removePreference(p);
            }
        }
    }

    private void setupStore(long purchaseTimestamp) {
        /*
        Preference p = findPreference("frostwire.prefs.offers.buy_no_ads");
        if (p != null && !Constants.IS_GOOGLE_PLAY_DISTRIBUTION) {
            PreferenceScreen s = getPreferenceScreen();
            s.removePreference(p);
        } else if (p != null) {
            final PlayStore playStore = PlayStore.getInstance();
            playStore.refresh();
            final Collection<Product> purchasedProducts = Products.listEnabled(playStore, Products.DISABLE_ADS_FEATURE);
            if (purchaseTimestamp == 0 && purchasedProducts != null && purchasedProducts.size() > 0) {
                initRemoveAdsSummaryWithPurchaseInfo(p, purchasedProducts);
                //otherwise, a BuyActivity intent has been configured on application_preferences.xml
            } else if (purchaseTimestamp > 0 &&
                    (System.currentTimeMillis()-purchaseTimestamp) < 30000) {
                p.setSummary(getString(R.string.processing_payment)+"...");
                p.setOnPreferenceClickListener(null);
            } else {
                p.setSummary(R.string.remove_ads_description);
                p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        PlayStore.getInstance().endAsync();
                        Intent intent = new Intent(SettingsActivity.this, BuyActivity.class);
                        startActivityForResult(intent, BuyActivity.PURCHASE_SUCCESSFUL_RESULT_CODE);
                        return true;
                    }
                });
            }
        }
        */
    }

    @Override
    public void startActivity(Intent intent) {
        if (intent != null && StoragePicker.ACTION_OPEN_DOCUMENT_TREE.equals(intent.getAction())) {
            StoragePicker.show(this);
        } else {
            super.startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        /*if (requestCode == StoragePicker.SELECT_FOLDER_REQUEST_CODE) {
            StoragePreference.onDocumentTreeActivityResult(this, requestCode, resultCode, data);
        } else if (requestCode == BuyActivity.PURCHASE_SUCCESSFUL_RESULT_CODE &&
                data != null &&
                data.hasExtra(BuyActivity.EXTRA_KEY_PURCHASE_TIMESTAMP)) {
            // We (onActivityResult) are invoked before onResume()
            removeAdsPurchaseTime = data.getLongExtra(BuyActivity.EXTRA_KEY_PURCHASE_TIMESTAMP, 0);
            LOG.info("onActivityResult: User just purchased something. removeAdsPurchaseTime="+removeAdsPurchaseTime);
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
        */
    }

    private void connect() {
        final Activity context = this;
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                //Engine.instance().startServices();

                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SwitchPreference preference = (SwitchPreference) findPreference("jdonkey.prefs.internal.connect_disconnect");
                        connectSwitchImOnIt(preference);
                    }
                });

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                UIUtils.showShortMessage(context, R.string.toast_on_connect);
                updateConnectSwitch();
            }
        };

        task.execute();
    }

    private void disconnect() {
        final Context context = this;
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                //Engine.instance().stopServices(true);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                UIUtils.showShortMessage(context, R.string.toast_on_disconnect);
                updateConnectSwitch();
            }
        };

        task.execute();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        LOG.info("on pref tree click");
        boolean r = super.onPreferenceTreeClick(preferenceScreen, preference);
        if (preference instanceof PreferenceScreen) {
            LOG.info("pref is screen");
            initializePreferenceScreen((PreferenceScreen) preference);
            currentPreferenceKey = preference.getKey();
        }
        return r;
    }

    /**
     * HOW TO HIDE A NESTED PreferenceScreen ActionBar icon.
     * The nested PreferenceScreens are basically Dialog instances,
     * if we want to hide the icon on those, we need to get their dialog.getActionBar()
     * instance, hide the icon, and then we need to set the click listeners for the
     * dialog's laid out views. Here we do all that.
     *
     * @param preferenceScreen
     */
    private void initializePreferenceScreen(PreferenceScreen preferenceScreen) {
        if (preferenceScreen == null) {
            return;
        }

        final Dialog dialog = preferenceScreen.getDialog();
        if (dialog != null) {

            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                    if (finishOnBack) {
                        finish();
                    }
                }
            });

            hideActionBarIcon(dialog.getActionBar());
            View homeButton = dialog.findViewById(android.R.id.home);
            LOG.info("home btn: {}", homeButton!=null?"yes":"no");

            if (homeButton != null) {
                OnClickListener dismissDialogClickListener = new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (finishOnBack) {
                            finish();
                            return;
                        }
                        dialog.dismiss();
                    }
                };

                ViewParent homeBtnContainer = homeButton.getParent();
                if (homeBtnContainer instanceof FrameLayout) {
                    ViewGroup containerParent = (ViewGroup) homeBtnContainer.getParent();

                    if (containerParent instanceof LinearLayout) {
                        containerParent.setOnClickListener(dismissDialogClickListener);
                    } else {
                        ((FrameLayout) homeBtnContainer).setOnClickListener(dismissDialogClickListener);
                    }
                } else {
                    homeButton.setOnClickListener(dismissDialogClickListener);
                }
            }
        }
    }

    private void openPreference(String key) {
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        final ListAdapter listAdapter = preferenceScreen.getRootAdapter();

        final int itemsCount = listAdapter.getCount();
        int itemNumber;
        for (itemNumber = 0; itemNumber < itemsCount; ++itemNumber) {
            if (listAdapter.getItem(itemNumber).equals(findPreference(key))) {
                preferenceScreen.onItemClick(null, null, itemNumber, 0);
                break;
            }
        }
    }
}
