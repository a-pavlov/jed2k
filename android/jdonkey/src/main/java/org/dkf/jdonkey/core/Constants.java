/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2016, jed2k(R). All rights reserved.
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

package org.dkf.jdonkey.core;

import org.dkf.jed2k.BuildConfig;

/**
 * Static class containing all constants in one place.
 *
 * @author gubatron
 * @author aldenml
 */
public final class Constants {

    private Constants() {
    }

    public static final boolean IS_GOOGLE_PLAY_DISTRIBUTION = BuildConfig.FLAVOR.equals("basic");

    private static final String BUILD_PREFIX = !IS_GOOGLE_PLAY_DISTRIBUTION ? "1000" : "";

    /**
     * should manually match the manifest, here for convenience so we can ask for it from static contexts without
     * needing to pass the Android app context to obtain the PackageManager instance.
     */
    public static final String JED2K_BUILD = BUILD_PREFIX + (BuildConfig.VERSION_CODE % 1000);

    public static final String APP_PACKAGE_NAME = "org.dkf.jed2k";

    public static final String JED2K_VERSION_STRING = BuildConfig.VERSION_NAME;

    // preference keys
    static final String PREF_KEY_CORE_UUID = "jed2k.prefs.core.uuid";
    public static final String PREF_KEY_CORE_LAST_SEEN_VERSION = "jed2k.prefs.core.last_seen_version";

    public static final String PREF_KEY_NETWORK_ENABLE_DHT = "jed2k.prefs.network.enable_dht";
    public static final String PREF_KEY_NETWORK_USE_MOBILE_DATA = "jed2k.prefs.network.use_mobile_data";
    public static final String PREF_KEY_NETWORK_MAX_CONCURRENT_UPLOADS = "jed2k.prefs.network.max_concurrent_uploads";

    public static final String PREF_KEY_SEARCH_COUNT_DOWNLOAD_FOR_TORRENT_DEEP_SCAN = "jed2k.prefs.search.count_download_for_torrent_deep_scan";
    public static final String PREF_KEY_SEARCH_COUNT_ROUNDS_FOR_TORRENT_DEEP_SCAN = "jed2k.prefs.search.count_rounds_for_torrent_deep_scan";
    public static final String PREF_KEY_SEARCH_INTERVAL_MS_FOR_TORRENT_DEEP_SCAN = "jed2k.prefs.search.interval_ms_for_torrent_deep_scan";
    public static final String PREF_KEY_SEARCH_MIN_SEEDS_FOR_TORRENT_DEEP_SCAN = "jed2k.prefs.search.min_seeds_for_torrent_deep_scan";
    public static final String PREF_KEY_SEARCH_MIN_SEEDS_FOR_TORRENT_RESULT = "jed2k.prefs.search.min_seeds_for_torrent_result";
    public static final String PREF_KEY_SEARCH_MAX_TORRENT_FILES_TO_INDEX = "jed2k.prefs.search.max_torrent_files_to_index";
    public static final String PREF_KEY_SEARCH_FULLTEXT_SEARCH_RESULTS_LIMIT = "jed2k.prefs.search.fulltext_search_results_limit";

    public static final String PREF_KEY_SEARCH_USE_EXTRATORRENT = "jed2k.prefs.search.use_extratorrent";
    public static final String PREF_KEY_SEARCH_USE_MININOVA = "jed2k.prefs.search.use_mininova";
    public static final String PREF_KEY_SEARCH_USE_VERTOR = "jed2k.prefs.search.use_vertor";
    public static final String PREF_KEY_SEARCH_USE_YOUTUBE = "jed2k.prefs.search.use_youtube";
    public static final String PREF_KEY_SEARCH_USE_SOUNDCLOUD = "jed2k.prefs.search.use_soundcloud";
    public static final String PREF_KEY_SEARCH_USE_ARCHIVEORG = "jed2k.prefs.search.use_archiveorg";
    public static final String PREF_KEY_SEARCH_USE_FROSTCLICK = "jed2k.prefs.search.use_frostclick";
    public static final String PREF_KEY_SEARCH_USE_BITSNOOP = "jed2k.prefs.search.use_bitsnoop";
    public static final String PREF_KEY_SEARCH_USE_TORLOCK = "jed2k.prefs.search.use_torlock";
    public static final String PREF_KEY_SEARCH_USE_TORRENTDOWNLOADS = "jed2k.prefs.search.use_torrentdownloads";
    public static final String PREF_KEY_SEARCH_USE_EZTV = "jed2k.prefs.search.use_eztv";
    public static final String PREF_KEY_SEARCH_USE_APPIA = "jed2k.prefs.search.use_appia";
    public static final String PREF_KEY_SEARCH_USE_TPB = "jed2k.prefs.search.use_tpb";
    public static final String PREF_KEY_SEARCH_USE_MONOVA = "jed2k.prefs.search.use_monova";
    public static final String PREF_KEY_SEARCH_USE_YIFY = "jed2k.prefs.search.use_yify";
    public static final String PREF_KEY_SEARCH_USE_TORRENTSFM = "jed2k.prefs.search.use_torrentsfm";
    public static final String PREF_KEY_SEARCH_USE_BTJUNKIE = "jed2k.prefs.search.use_btjunkie";

    public static final String PREF_KEY_SEARCH_PREFERENCE_CATEGORY = "jed2k.prefs.search.preference_category";
    public static final String PREF_KEY_OTHER_PREFERENCE_CATEGORY = "jed2k.prefs.other.preference_category";

    public static final String PREF_KEY_GUI_VIBRATE_ON_FINISHED_DOWNLOAD = "jed2k.prefs.gui.vibrate_on_finished_download";
    public static final String PREF_KEY_GUI_LAST_MEDIA_TYPE_FILTER = "jed2k.prefs.gui.last_media_type_filter";
    public static final String PREF_KEY_GUI_TOS_ACCEPTED = "jed2k.prefs.gui.tos_accepted";
    public static final String PREF_KEY_GUI_ALREADY_RATED_US_IN_MARKET = "jed2k.prefs.gui.already_rated_in_market";
    public static final String PREF_KEY_GUI_FINISHED_DOWNLOADS_BETWEEN_RATINGS_REMINDER = "jed2k.prefs.gui.finished_downloads_between_ratings_reminder";
    public static final String PREF_KEY_GUI_INITIAL_SETTINGS_COMPLETE = "jed2k.prefs.gui.initial_settings_complete";
    public static final String PREF_KEY_GUI_ENABLE_PERMANENT_STATUS_NOTIFICATION = "jed2k.prefs.gui.enable_permanent_status_notification";
    public static final String PREF_KEY_GUI_SHOW_TRANSFERS_ON_DOWNLOAD_START = "jed2k.prefs.gui.show_transfers_on_download_start";
    public static final String PREF_KEY_GUI_SHOW_NEW_TRANSFER_DIALOG = "jed2k.prefs.gui.show_new_transfer_dialog";
    public static final String PREF_KEY_GUI_USE_APPLOVIN = "jed2k.prefs.gui.use_applovin";
    public static final String PREF_KEY_GUI_USE_INMOBI = "jed2k.prefs.gui.use_inmobi";
    public static final String PREF_KEY_GUI_USE_MOBFOX = "jed2k.prefs.gui.use_mobfox";
    public static final String PREF_KEY_GUI_USE_REMOVEADS = "jed2k.prefs.gui.use_removeads";
    public static final String PREF_KEY_GUI_REMOVEADS_BACK_TO_BACK_THRESHOLD = "jed2k.prefs.gui.removeads_back_to_back_threshold";
    public static final String PREF_KEY_GUI_SUPPORT_jed2k = "jed2k.prefs.gui.support_fw2";
    public static final String PREF_KEY_GUI_INTERSTITIAL_OFFERS_TRANSFER_STARTS = "jed2k.prefs.gui.interstitial_offers_transfer_starts";
    public static final String PREF_KEY_GUI_INTERSTITIAL_TRANSFER_OFFERS_TIMEOUT_IN_MINUTES = "jed2k.prefs.gui.interstitial_transfer_offers_timeout_in_minutes";
    public static final String PREF_KEY_GUI_OFFERS_WATERFALL = "jed2k.prefs.gui.offers_waterfall";
    public static final String PREF_KEY_ADNETWORK_ASK_FOR_LOCATION_PERMISSION = "jed2k.prefs.gui.adnetwork_ask_for_location";

    public static final String PREF_KEY_TORRENT_MAX_DOWNLOAD_SPEED = "jed2k.prefs.torrent.max_download_speed";
    public static final String PREF_KEY_TORRENT_MAX_UPLOAD_SPEED = "jed2k.prefs.torrent.max_upload_speed";
    public static final String PREF_KEY_TORRENT_MAX_DOWNLOADS = "jed2k.prefs.torrent.max_downloads";
    public static final String PREF_KEY_TORRENT_MAX_UPLOADS = "jed2k.prefs.torrent.max_uploads";
    public static final String PREF_KEY_TORRENT_MAX_TOTAL_CONNECTIONS = "jed2k.prefs.torrent.max_total_connections";
    public static final String PREF_KEY_TORRENT_MAX_PEERS = "jed2k.prefs.torrent.max_peers";
    public static final String PREF_KEY_TORRENT_SEED_FINISHED_TORRENTS = "jed2k.prefs.torrent.seed_finished_torrents";
    public static final String PREF_KEY_TORRENT_SEED_FINISHED_TORRENTS_WIFI_ONLY = "jed2k.prefs.torrent.seed_finished_torrents_wifi_only";

    public static final String PREF_KEY_STORAGE_PATH = "jed2k.prefs.storage.path";

    public static final String PREF_KEY_UXSTATS_ENABLED = "jed2k.prefs.uxstats.enabled";

    public static final String ACTION_REQUEST_SHUTDOWN = "org.dkf.jdonkey.ACTION_REQUEST_SHUTDOWN";
    public static final String ACTION_SHOW_TRANSFERS = "org.dkf.jdonkey.ACTION_SHOW_TRANSFERS";
    public static final String ACTION_SHOW_VPN_STATUS_PROTECTED = "org.dkf.jdonkey.ACTION_SHOW_VPN_STATUS_PROTECTED";
    public static final String ACTION_SHOW_VPN_STATUS_UNPROTECTED = "org.dkf.jdonkey.ACTION_SHOW_VPN_STATUS_UNPROTECTED";
    public static final String ACTION_START_TRANSFER_FROM_PREVIEW = "org.dkf.jdonkey.ACTION_START_TRANSFER_FROM_PREVIEW";
    public static final String ACTION_MEDIA_PLAYER_PLAY = "org.dkf.jdonkey.ACTION_MEDIA_PLAYER_PLAY";
    public static final String ACTION_MEDIA_PLAYER_STOPPED = "org.dkf.jdonkey.ACTION_MEDIA_PLAYER_STOPPED";
    public static final String ACTION_MEDIA_PLAYER_PAUSED = "org.dkf.jdonkey.ACTION_MEDIA_PLAYER_PAUSED";
    public static final String ACTION_REFRESH_FINGER = "org.dkf.jdonkey.ACTION_REFRESH_FINGER";
    public static final String ACTION_SETTINGS_SELECT_STORAGE = "org.dkf.jdonkey.ACTION_SETTINGS_SELECT_STORAGE";
    public static final String ACTION_SETTINGS_OPEN_TORRENT_SETTINGS = "org.dkf.jdonkey.ACTION_SETTINGS_OPEN_TORRENT_SETTINGS";
    public static final String ACTION_NOTIFY_SDCARD_MOUNTED = "org.dkf.jdonkey.ACTION_NOTIFY_SDCARD_MOUNTED";
    public static final String ACTION_FILE_ADDED_OR_REMOVED = "org.dkf.jdonkey.ACTION_FILE_ADDED_OR_REMOVED";
    public static final String EXTRA_DOWNLOAD_COMPLETE_NOTIFICATION = "org.dkf.jdonkey.EXTRA_DOWNLOAD_COMPLETE_NOTIFICATION";
    public static final String EXTRA_DOWNLOAD_COMPLETE_PATH = "org.dkf.jdonkey.EXTRA_DOWNLOAD_COMPLETE_PATH";
    public static final String EXTRA_REFRESH_FILE_TYPE = "org.dkf.jdonkey.EXTRA_REFRESH_FILE_TYPE";
    public static final String EXTRA_FINISH_MAIN_ACTIVITY = "org.dkf.jdonkey.EXTRA_FINISH_MAIN_ACTIVITY";

    public static final String BROWSE_PEER_FRAGMENT_LISTVIEW_FIRST_VISIBLE_POSITION = "org.dkf.jdonkey.BROWSE_PEER_FRAGMENT_LISTVIEW_FIRST_VISIBLE_POSITION.";

    public static final int NOTIFICATION_DOWNLOAD_TRANSFER_FINISHED = 1001;

    // generic file types
    public static final byte FILE_TYPE_AUDIO = 0x00;
    public static final byte FILE_TYPE_PICTURES = 0x01;
    public static final byte FILE_TYPE_VIDEOS = 0x02;
    public static final byte FILE_TYPE_DOCUMENTS = 0x03;
    public static final byte FILE_TYPE_APPLICATIONS = 0x04;
    public static final byte FILE_TYPE_RINGTONES = 0x05;
    public static final byte FILE_TYPE_TORRENTS = 0x06;

    public static final String MIME_TYPE_ANDROID_PACKAGE_ARCHIVE = "application/vnd.android.package-archive";
    public static final String MIME_TYPE_BITTORRENT = "application/x-bittorrent";

    /**
     * URL where jed2k checks for software updates
     */
    private static final String FROM_URL_PARAMETERS = "from=android&basic=" + (IS_GOOGLE_PLAY_DISTRIBUTION ? "1" : "0") + "&version=" + JED2K_VERSION_STRING + "&build=" + JED2K_BUILD;
    public static final String SERVER_UPDATE_URL = "http://update.jed2k.com/android?" + FROM_URL_PARAMETERS;
    public static final String jed2k_PLUS_URL = "http://www.jed2k.com/android?" + FROM_URL_PARAMETERS;
    public static final String SERVER_PROMOTIONS_URL = "http://update.jed2k.com/o.php?" + FROM_URL_PARAMETERS;
    public static final String SUPPORT_URL = "http://support.jed2k.com/hc/en-us/categories/200014385-jed2k-for-Android";
    public static final String TERMS_OF_USE_URL = "http://www.jed2k.com/terms";

    public static final String USER_AGENT = "jed2k/android-" + (Constants.IS_GOOGLE_PLAY_DISTRIBUTION ? "basic" : "plus" ) + "/" + Constants.JED2K_VERSION_STRING + "/" + Constants.JED2K_BUILD;

    public static final long LIBRARIAN_FILE_COUNT_CACHE_TIMEOUT = 2 * 60 * 1000; // 2 minutes

    public static final String INMOBI_INTERSTITIAL_PROPERTY_ID = "c1e6be702d614523b725af8b86f99e8f";
    public static final String MOBFOX_INVENTORY_HASH = "cc73727fabc4235d769120f8a1d0635d";

    public static final int NOTIFIED_BLOOM_FILTER_BITSET_SIZE = 320000; //40 kilobytes
    public static final int NOTIFIED_BLOOM_FILTER_EXPECTED_ELEMENTS = 10000;

    /**
     * Social Media official URLS
     */
    public static final String SOCIAL_URL_FACEBOOK_PAGE = "https://www.facebook.com/jed2kOfficial";
    public static final String SOCIAL_URL_TWITTER_PAGE = "https://twitter.com/jed2k";
    public static final String SOCIAL_URL_REDDIT_PAGE = "https://reddit.com/r/jed2k";

    public static final String VPN_LEARN_MORE_URL = "http://www.jed2k.com/vpn.expressvpn.learnmore";
    public static final String EXPRESSVPN_URL_BASIC = "http://www.jed2k.com/vpn.expressvpn";
    public static final String EXPRESSVPN_URL_PLUS = "http://www.jed2k.com/vpn.expressvpn";
    public static final float EXPRESSVPN_STARTING_USD_PRICE = 8.32f;

    public static final String jed2k_GIVE_URL = "http://www.jed2k.com/give/?from=";

    public static final String AD_NETWORK_SHORTCODE_APPLOVIN = "AL";
    public static final String AD_NETWORK_SHORTCODE_INMOBI = "IM";
    public static final String AD_NETWORK_SHORTCODE_REMOVEADS = "RA";
    public static final String AD_NETWORK_SHORTCODE_MOBFOX = "MF";
}
