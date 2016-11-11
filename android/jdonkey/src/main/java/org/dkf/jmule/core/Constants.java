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

package org.dkf.jmule.core;

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

    public static final String APP_PACKAGE_NAME = "org.dkf.jmule";

    public static final String JED2K_VERSION_STRING = BuildConfig.VERSION_NAME;

    // preference keys
    public static final String PREF_KEY_CORE_CONNECTED = "jmule.prefs.internal.connect_disconnect";
    static final String PREF_KEY_CORE_UUID = "jmule.prefs.core.uuid";
    public static final String PREF_KEY_CORE_LAST_SEEN_VERSION = "jmule.prefs.core.last_seen_version";

    public static final String PREF_KEY_NETWORK_MAX_CONCURRENT_UPLOADS = "jmule.prefs.network.max_concurrent_uploads";

    public static final String PREF_KEY_SERVER_ID = "jmule.prefs.server.id";
    public static final String PREF_KEY_SERVER_HOST = "jmule.prefs.server.host";
    public static final String PREF_KEY_SERVER_PORT = "jmule.prefs.server.port";

    public static final String PREF_KEY_SEARCH_COUNT_DOWNLOAD_FOR_TORRENT_DEEP_SCAN = "jmule.prefs.search.count_download_for_torrent_deep_scan";
    public static final String PREF_KEY_SEARCH_COUNT_ROUNDS_FOR_TORRENT_DEEP_SCAN = "jmule.prefs.search.count_rounds_for_torrent_deep_scan";
    public static final String PREF_KEY_SEARCH_INTERVAL_MS_FOR_TORRENT_DEEP_SCAN = "jmule.prefs.search.interval_ms_for_torrent_deep_scan";
    public static final String PREF_KEY_SEARCH_MIN_SEEDS_FOR_TORRENT_DEEP_SCAN = "jmule.prefs.search.min_seeds_for_torrent_deep_scan";
    public static final String PREF_KEY_SEARCH_MIN_SEEDS_FOR_TORRENT_RESULT = "jmule.prefs.search.min_seeds_for_torrent_result";
    public static final String PREF_KEY_SEARCH_MAX_TORRENT_FILES_TO_INDEX = "jmule.prefs.search.max_torrent_files_to_index";
    public static final String PREF_KEY_SEARCH_FULLTEXT_SEARCH_RESULTS_LIMIT = "jmule.prefs.search.fulltext_search_results_limit";

    public static final String PREF_KEY_OTHER_PREFERENCE_CATEGORY = "jmule.prefs.other.preference_category";

    public static final String PREF_KEY_GUI_VIBRATE_ON_FINISHED_DOWNLOAD = "jmule.prefs.gui.vibrate_on_finished_download";
    public static final String PREF_KEY_GUI_LAST_MEDIA_TYPE_FILTER = "jmule.prefs.gui.last_media_type_filter";
    public static final String PREF_KEY_GUI_TOS_ACCEPTED = "jmule.prefs.gui.tos_accepted";
    public static final String PREF_KEY_GUI_ALREADY_RATED_US_IN_MARKET = "jmule.prefs.gui.already_rated_in_market";
    public static final String PREF_KEY_GUI_FINISHED_DOWNLOADS_BETWEEN_RATINGS_REMINDER = "jmule.prefs.gui.finished_downloads_between_ratings_reminder";
    public static final String PREF_KEY_GUI_INITIAL_SETTINGS_COMPLETE = "jmule.prefs.gui.initial_settings_complete";
    public static final String PREF_KEY_GUI_ENABLE_PERMANENT_STATUS_NOTIFICATION = "jmule.prefs.gui.enable_permanent_status_notification";
    public static final String PREF_KEY_GUI_SHOW_TRANSFERS_ON_DOWNLOAD_START = "jmule.prefs.gui.show_transfers_on_download_start";
    public static final String PREF_KEY_GUI_SHOW_NEW_TRANSFER_DIALOG = "jmule.prefs.gui.show_new_transfer_dialog";

    // ed2k common and servers
    public static final String PREF_KEY_USER_AGENT = "jmule.prefs.user_agent";
    public static final String PREF_KEY_NICKNAME = "jmule.prefs.nickname";
    public static final String PREF_KEY_LISTEN_PORT = "jmule.prefs.listen_port";
    public static final String PREF_KEY_CONN_SERVER_ON_START = "jmule.prefs.connect_server_on_start";
    public static final String PREF_KEY_SHOW_SERVER_MSG = "jmule.prefs.show_servers_messages";
    public static final String PREF_KEY_SERVERS_LIST = "jmule.prefs.servers_list";
    public static final String PREF_KEY_AUTO_START_SERVICE = "jmule.prefs.gui.auto_start_service";
    public static final String PREF_KEY_FORWARD_PORTS = "jmule.prefs.gui.forward_ports";


    public static final String PREF_KEY_TRANSFER_PREFERENCE_CATEGORY = "jmule.prefs.transfer.preference_category";
    public static final String PREF_KEY_TRANSFER_MAX_DOWNLOADS = "jmule.prefs.transfer.max_downloads";
    public static final String PREF_KEY_TRANSFER_MAX_TOTAL_CONNECTIONS = "jmule.prefs.transfer.max_total_connections";
    public static final String PREF_KEY_NETWORK_USE_MOBILE_DATA = "jmule.prefs.network.use_mobile_data";


    public static final String PREF_KEY_STORAGE_PATH = "jmule.prefs.storage.path";

    public static final String ACTION_SHOW_VPN_STATUS_PROTECTED = "org.dkf.jmule.ACTION_SHOW_VPN_STATUS_PROTECTED";
    public static final String ACTION_SHOW_VPN_STATUS_UNPROTECTED = "org.dkf.jmule.ACTION_SHOW_VPN_STATUS_UNPROTECTED";
    public static final String ACTION_START_TRANSFER_FROM_PREVIEW = "org.dkf.jmule.ACTION_START_TRANSFER_FROM_PREVIEW";
    public static final String ACTION_MEDIA_PLAYER_PLAY = "org.dkf.jmule.ACTION_MEDIA_PLAYER_PLAY";
    public static final String ACTION_MEDIA_PLAYER_STOPPED = "org.dkf.jmule.ACTION_MEDIA_PLAYER_STOPPED";
    public static final String ACTION_MEDIA_PLAYER_PAUSED = "org.dkf.jmule.ACTION_MEDIA_PLAYER_PAUSED";
    public static final String ACTION_REFRESH_FINGER = "org.dkf.jmule.ACTION_REFRESH_FINGER";
    public static final String ACTION_SETTINGS_SELECT_STORAGE = "org.dkf.jmule.ACTION_SETTINGS_SELECT_STORAGE";
    public static final String ACTION_SETTINGS_OPEN_TORRENT_SETTINGS = "org.dkf.jmule.ACTION_SETTINGS_OPEN_TORRENT_SETTINGS";
    public static final String ACTION_NOTIFY_SDCARD_MOUNTED = "org.dkf.jmule.ACTION_NOTIFY_SDCARD_MOUNTED";
    public static final String EXTRA_DOWNLOAD_COMPLETE_PATH = "org.dkf.jmule.EXTRA_DOWNLOAD_COMPLETE_PATH";
    public static final String EXTRA_REFRESH_FILE_TYPE = "org.dkf.jmule.EXTRA_REFRESH_FILE_TYPE";
    public static final String EXTRA_FINISH_MAIN_ACTIVITY = "org.dkf.jmule.EXTRA_FINISH_MAIN_ACTIVITY";

    public static final String BROWSE_PEER_FRAGMENT_LISTVIEW_FIRST_VISIBLE_POSITION = "org.dkf.jmule.BROWSE_PEER_FRAGMENT_LISTVIEW_FIRST_VISIBLE_POSITION.";

    public static final int NOTIFICATION_DOWNLOAD_TRANSFER_FINISHED = 1001;

    // generic file types
    public static final byte FILE_TYPE_AUDIO = 0x00;
    public static final byte FILE_TYPE_PICTURES = 0x01;
    public static final byte FILE_TYPE_VIDEOS = 0x02;
    public static final byte FILE_TYPE_DOCUMENTS = 0x03;
    public static final byte FILE_TYPE_APPLICATIONS = 0x04;
    public static final byte FILE_TYPE_TORRENTS = 0x06;
    public static final byte FILE_TYPE_ARCHIVE = 0x7;
    public static final byte FILE_TYPE_CD_IMAGE = 0x8;
    public static final byte FILE_TYPE_OTHERS = 0x9;

    public static final String MIME_TYPE_ANDROID_PACKAGE_ARCHIVE = "application/vnd.android.package-archive";
    public static final String MIME_TYPE_BITTORRENT = "application/x-bittorrent";


    public static final int NOTIFIED_BLOOM_FILTER_BITSET_SIZE = 320000; //40 kilobytes
    public static final int NOTIFIED_BLOOM_FILTER_EXPECTED_ELEMENTS = 10000;

    /**
     * Social Media official URLS
     */
    public static final String SOCIAL_URL_FACEBOOK_PAGE = "https://www.facebook.com/jed2kOfficial";
    public static final String SOCIAL_URL_TWITTER_PAGE = "https://twitter.com/jed2k";
    public static final String SOCIAL_URL_REDDIT_PAGE = "https://reddit.com/r/jed2k";
    public static final String GITHUB_PAGE = "https://github.com/a-pavlov/jed2k";

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
