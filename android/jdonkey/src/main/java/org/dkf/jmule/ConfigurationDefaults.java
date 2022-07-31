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

package org.dkf.jmule;

import android.os.Environment;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Hash;
import org.dkf.jed2k.protocol.server.ServerMet;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author gubatron
 * @author aldenml
 */
final class ConfigurationDefaults {

    private final Map<String, Object> defaultValues;
    private final Map<String, Object> resetValues;

    ConfigurationDefaults() {
        defaultValues = new HashMap<>();
        resetValues = new HashMap<>();
        load();
    }

    Map<String, Object> getDefaultValues() {
        return Collections.unmodifiableMap(defaultValues);
    }

    Map<String, Object> getResetValues() {
        return Collections.unmodifiableMap(resetValues);
    }

    private void load() {
        defaultValues.put(Constants.PREF_KEY_STORAGE_PATH, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
        defaultValues.put(Constants.PREF_KEY_CORE_UUID, uuidToByteArray(UUID.randomUUID()));
        defaultValues.put(Constants.PREF_KEY_CORE_LAST_SEEN_VERSION, "");//won't know until I see it.

        defaultValues.put(Constants.PREF_KEY_GUI_VIBRATE_ON_FINISHED_DOWNLOAD, true);
        defaultValues.put(Constants.PREF_KEY_GUI_LAST_MEDIA_TYPE_FILTER, Constants.FILE_TYPE_AUDIO);
        defaultValues.put(Constants.PREF_KEY_GUI_TOS_ACCEPTED, false);
        defaultValues.put(Constants.PREF_KEY_GUI_ALREADY_RATED_US_IN_MARKET, false);
        defaultValues.put(Constants.PREF_KEY_GUI_FINISHED_DOWNLOADS_BETWEEN_RATINGS_REMINDER, 10);
        defaultValues.put(Constants.PREF_KEY_GUI_INITIAL_SETTINGS_COMPLETE, false);
        defaultValues.put(Constants.PREF_KEY_GUI_ENABLE_PERMANENT_STATUS_NOTIFICATION, true);
        defaultValues.put(Constants.PREF_KEY_GUI_SHOW_TRANSFERS_ON_DOWNLOAD_START, true);
        defaultValues.put(Constants.PREF_KEY_GUI_SHOW_NEW_TRANSFER_DIALOG, true);
        defaultValues.put(Constants.PREF_KEY_GUI_SAFE_MODE, true);
        defaultValues.put(Constants.PREF_KEY_GUI_ALERTED_SAFE_MODE, false);
        defaultValues.put(Constants.PREF_KEY_GUI_SHARE_MEDIA_DOWNLOADS, false);

        defaultValues.put(Constants.PREF_KEY_SEARCH_COUNT_DOWNLOAD_FOR_TORRENT_DEEP_SCAN, 20);
        defaultValues.put(Constants.PREF_KEY_SEARCH_COUNT_ROUNDS_FOR_TORRENT_DEEP_SCAN, 10);
        defaultValues.put(Constants.PREF_KEY_SEARCH_INTERVAL_MS_FOR_TORRENT_DEEP_SCAN, 2000);
        defaultValues.put(Constants.PREF_KEY_SEARCH_MIN_SEEDS_FOR_TORRENT_DEEP_SCAN, 20); // this number must be bigger than PREF_KEY_SEARCH_MIN_SEEDS_FOR_TORRENT_RESULT to become relevant
        defaultValues.put(Constants.PREF_KEY_SEARCH_MIN_SEEDS_FOR_TORRENT_RESULT, 20);
        defaultValues.put(Constants.PREF_KEY_SEARCH_MAX_TORRENT_FILES_TO_INDEX, 100); // no ultra big torrents here
        defaultValues.put(Constants.PREF_KEY_SEARCH_FULLTEXT_SEARCH_RESULTS_LIMIT, 256);

        defaultValues.put(Constants.PREF_KEY_NETWORK_USE_MOBILE_DATA, true);
        defaultValues.put(Constants.PREF_KEY_NETWORK_USE_WIFI_ONLY, false);
        defaultValues.put(Constants.PREF_KEY_NETWORK_BITTORRENT_ON_VPN_ONLY, false);
        defaultValues.put(Constants.PREF_KEY_NETWORK_MAX_CONCURRENT_UPLOADS, 3);


        defaultValues.put(Constants.PREF_KEY_STORAGE_PATH, Environment.getExternalStorageDirectory().getAbsolutePath()); // /mnt/sdcard

        resetValue(Constants.PREF_KEY_SEARCH_COUNT_DOWNLOAD_FOR_TORRENT_DEEP_SCAN);
        resetValue(Constants.PREF_KEY_SEARCH_COUNT_ROUNDS_FOR_TORRENT_DEEP_SCAN);
        resetValue(Constants.PREF_KEY_SEARCH_INTERVAL_MS_FOR_TORRENT_DEEP_SCAN);
        resetValue(Constants.PREF_KEY_SEARCH_MIN_SEEDS_FOR_TORRENT_DEEP_SCAN);
        resetValue(Constants.PREF_KEY_SEARCH_MIN_SEEDS_FOR_TORRENT_RESULT);
        resetValue(Constants.PREF_KEY_SEARCH_MAX_TORRENT_FILES_TO_INDEX);
        resetValue(Constants.PREF_KEY_SEARCH_FULLTEXT_SEARCH_RESULTS_LIMIT);


        defaultValues.put(Constants.PREF_KEY_NICKNAME, "Nickname");
        defaultValues.put(Constants.PREF_KEY_LISTEN_PORT, 30000l);
        defaultValues.put(Constants.PREF_KEY_TRANSFER_MAX_TOTAL_CONNECTIONS, 100l);
        defaultValues.put(Constants.PREF_KEY_CONN_SERVER_ON_START, false);
        defaultValues.put(Constants.PREF_KEY_RECONNECT_TO_SERVER, false);
        defaultValues.put(Constants.PREF_KEY_PING_SERVER, false);
        defaultValues.put(Constants.PREF_KEY_SHOW_SERVER_MSG, true);
        defaultValues.put(Constants.PREF_KEY_AUTO_START_SERVICE, false);
        defaultValues.put(Constants.PREF_KEY_FORWARD_PORTS, false);

        defaultValues.put(Constants.PREF_KEY_CONNECT_DHT, false);

        // servers section
        ServerMet sm = new ServerMet();
        try {
            sm.addServer(ServerMet.ServerMetEntry.create("5.45.85.226", 6584, "eMule Security", "www.emule-security.org"));
            sm.addServer(ServerMet.ServerMetEntry.create("176.123.5.89", 4725, "eMule Sunrise", "Not perfect, but real"));
            sm.addServer(ServerMet.ServerMetEntry.create("46.105.126.71", 4661, "GrupoTS Server", "El foro de las series"));
            sm.addServer(ServerMet.ServerMetEntry.create("37.221.65.76", 4232, "!! Sharing-Devils No.2 !!", "https://forum.sharing-devils.to"));
            sm.addServer(ServerMet.ServerMetEntry.create("213.252.245.239", 43333, "Astra-3", "Astra-3"));
            sm.addServer(ServerMet.ServerMetEntry.create("95.217.134.86", 22888, "Astra-2", "Astra-2 Server"));
            sm.addServer(ServerMet.ServerMetEntry.create("185.105.3.69", 9191, "eDonkey Server No1", "eDonkey Server No1"));
            sm.addServer(ServerMet.ServerMetEntry.create("92.38.163.210", 35037, "Astra-6", "Astra-6 Server"));
            sm.addServer(ServerMet.ServerMetEntry.create("213.252.245.239", 33333, "Astra-5", "Astra-5 Server"));
            sm.addServer(ServerMet.ServerMetEntry.create("45.142.215.35", 42011, "Pentium Pilat 2022", "Pentium Pilat 2022 Server"));
            sm.addServer(ServerMet.ServerMetEntry.create("92.38.184.138", 51127, "Astra-1", "Astra-1 Server"));
            sm.addServer(ServerMet.ServerMetEntry.create("5.188.6.125", 31031, "Gaal", "Gaal Server"));
            sm.addServer(ServerMet.ServerMetEntry.create("185.105.3.69", 9797, "eDonkey Server No2", "eDonkey Server No2"));
            sm.addServer(ServerMet.ServerMetEntry.create("180.166.24.38", 14142, "Poor-eServer-1", "Poor-eServer-1"));
            defaultValues.put(Constants.PREF_KEY_SERVERS_LIST, sm);
        } catch(JED2KException e) {
            // wtf?
        }

        defaultValues.put(Constants.PREF_KEY_USER_AGENT, Hash.random(true).toString());
    }

    private void resetValue(String key) {
        resetValues.put(key, defaultValues.get(key));
    }

    private static byte[] uuidToByteArray(UUID uuid) {
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        byte[] buffer = new byte[16];

        for (int i = 0; i < 8; i++) {
            buffer[i] = (byte) (msb >>> 8 * (7 - i));
        }
        for (int i = 8; i < 16; i++) {
            buffer[i] = (byte) (lsb >>> 8 * (7 - i));
        }

        return buffer;
    }
}
