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

package org.dkf.jmule;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.util.Hex;
import org.dkf.jed2k.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Looking for default config values? look at {@link ConfigurationDefaults}
 *
 * @author gubatron
 * @author aldenml
 */
public class ConfigurationManager {
    private static final Logger log = LoggerFactory.getLogger(ConfigurationManager.class);
    private final SharedPreferences preferences;
    private final Editor editor;

    private final ConfigurationDefaults defaults;

    private static ConfigurationManager instance;

    public synchronized static void create(Context context) {
        if (instance != null) {
            return;
        }
        instance = new ConfigurationManager(context);
    }

    public static ConfigurationManager instance() {
        if (instance == null) {
            throw new RuntimeException("ConfigurationManager not created");
        }
        return instance;
    }

    private ConfigurationManager(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = preferences.edit();

        defaults = new ConfigurationDefaults();
        initPreferences();
    }

    public String getString(String key) {
        return preferences.getString(key, "");
    }

    public void setString(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public int getInt(String key) {
        return preferences.getInt(key, 0);
    }

    public void setInt(String key, int value) {
        editor.putInt(key, value);
        editor.commit();
    }

    public long getLong(String key) {
        return preferences.getLong(key, 0);
    }

    public void setLong(String key, long value) {
        editor.putLong(key, value);
        editor.commit();
    }

    public boolean getBoolean(String key) {
        return preferences.getBoolean(key, false);
    }

    public void setBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }

    public File getFile(String key) {
        return new File(preferences.getString(key, ""));
    }

    private void setFile(String key, File value) {
        editor.putString(key, value.getAbsolutePath());
        editor.commit();
    }

    public byte[] getByteArray(String key) {
        String str = getString(key);

        if (StringUtils.isNullOrEmpty(str)) {
            return null;
        }

        try {
            return Hex.decode(str);
        } catch (Exception e) {
            return null;
        }
    }

    private void setByteArray(String key, final byte[] value) {
        setString(key, new String(Hex.encode(value)));
    }

    public void resetToDefaults() {
        resetToDefaults(defaults.getDefaultValues());
    }

    private void resetToDefault(String key) {
        if (defaults != null) {
            Map<String, Object> defaultValues = defaults.getDefaultValues();
            if (defaultValues != null && defaultValues.containsKey(key)) {
                Object defaultValue = defaultValues.get(key);
                initPreference(key, defaultValue, true);
            }
        }
    }

    public String getUUIDString() {
        return getString(Constants.PREF_KEY_CORE_UUID);
    }

    public int getLastMediaTypeFilter() {
        return getInt(Constants.PREF_KEY_GUI_LAST_MEDIA_TYPE_FILTER);
    }

    public void setLastMediaTypeFilter(int mediaTypeId) {
        setInt(Constants.PREF_KEY_GUI_LAST_MEDIA_TYPE_FILTER, mediaTypeId);
    }

    public boolean vibrateOnFinishedDownload() {
        return getBoolean(Constants.PREF_KEY_GUI_VIBRATE_ON_FINISHED_DOWNLOAD);
    }

    public <T extends Serializable> void setSerializable(final String key, T value) {
        ByteBuffer buffer = ByteBuffer.allocate(value.bytesCount());
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        try {
            value.put(buffer);
            byte data[] = new byte[buffer.capacity()];
            buffer.flip();
            log.info("set serializable {}", buffer.limit());
            buffer.get(data);
            setByteArray(key, data);
        } catch(JED2KException e) {
            log.error("set serialize failed {}", e);
        }
    }

    public <T extends Serializable> T getSerializable(final String key, T value) {
        byte[] data = getByteArray(key);
        if (data != null) {
            ByteBuffer buffer = ByteBuffer.wrap(data);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            try {
                value.get(buffer);
                return value;
            } catch(JED2KException e) {
                log.error("get serialize failed {}", e);
            }
        } else {
            log.warn("get serializable data array is null");
        }

        return null;
    }


    // TODO - refactor that methods to standard emule archives before usage
    /*
    public String[] getStringArray(String key) {
        return null;

        String jsonStringArray = preferences.getString(key, null);
        if (jsonStringArray == null) {
            return null;
        }
        return JsonUtils.toObject(jsonStringArray, String[].class);
    }
    */


    public void setStringArray(String key, String[] values) {
        /*editor.putString(key, JsonUtils.toJson(values));
        editor.commit();
        */
    }



    public int maxConcurrentUploads() {
        return getInt(Constants.PREF_KEY_NETWORK_MAX_CONCURRENT_UPLOADS);
    }

    public boolean showTransfersOnDownloadStart() {
        return getBoolean(Constants.PREF_KEY_GUI_SHOW_TRANSFERS_ON_DOWNLOAD_START);
    }

    public void registerOnPreferenceChange(OnSharedPreferenceChangeListener listener) {
        preferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public void unregisterOnPreferenceChange(OnSharedPreferenceChangeListener listener) {
        preferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    public String getStoragePath() {
        return getString(Constants.PREF_KEY_STORAGE_PATH);
    }

    public void setStoragePath(String path) {
        log.info("set storage path {}", path);
        if (path != null && path.length() > 0) { // minor verifications
            setString(Constants.PREF_KEY_STORAGE_PATH, path);
        }
    }

    public boolean connectoToServerOnRestart() {
        return getBoolean(Constants.PREF_KEY_CONN_SERVER_ON_START);
    }

    public boolean getReconnectoToServerOnConnectionClosed() {
        return getBoolean(Constants.PREF_KEY_RECONNECT_TO_SERVER);
    }

    public void setLastServerConnection(final String id, final String host, int port) {
        setString(Constants.PREF_KEY_SERVER_ID, id);
        setString(Constants.PREF_KEY_SERVER_HOST, host);
        setInt(Constants.PREF_KEY_SERVER_PORT, port);
    }

    public String getLastServerConnectionId() {
        return getString(Constants.PREF_KEY_SERVER_ID);
    }

    public String getLastServerConnectionHost() {
        return getString(Constants.PREF_KEY_SERVER_HOST);
    }

    public int getLastServerConnectionPort() {
        return getInt(Constants.PREF_KEY_SERVER_PORT);
    }

    private void initPreferences() {
        for (Entry<String, Object> entry : defaults.getDefaultValues().entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            initPreference(key, value, false);
        }

        //there are some configuration values that need to be reset every time to a desired value
        resetToDefaults(defaults.getResetValues());
    }

    private void initPreference(String key, Object value, boolean force) {
        if (value instanceof String) {
            initStringPreference(key, (String) value, force);
        } else if (value instanceof Integer) {
            initIntPreference(key, (Integer) value, force);
        } else if (value instanceof Long) {
            initLongPreference(key, (Long) value, force);
        } else if (value instanceof Boolean) {
            initBooleanPreference(key, (Boolean) value, force);
        } else if (value instanceof byte[]) {
            initByteArrayPreference(key, (byte[]) value, force);
        } else if (value instanceof File) {
            initFilePreference(key, (File) value, force);
        } else if (value instanceof String[]) {
            initStringArrayPreference(key, (String[]) value, force);
        } else if (value instanceof Serializable) {
            initSerializable(key, (Serializable)value, force);
        }
    }

    private void initStringPreference(String prefKeyName, String defaultValue, boolean force) {
        if (!preferences.contains(prefKeyName) || force) {
            setString(prefKeyName, defaultValue);
        }
    }

    private void initByteArrayPreference(String prefKeyName, byte[] defaultValue, boolean force) {
        if (!preferences.contains(prefKeyName) || force) {
            setByteArray(prefKeyName, defaultValue);
        }
    }

    private void initBooleanPreference(String prefKeyName, boolean defaultValue, boolean force) {
        if (!preferences.contains(prefKeyName) || force) {
            setBoolean(prefKeyName, defaultValue);
        }
    }

    private void initIntPreference(String prefKeyName, int defaultValue, boolean force) {
        if (!preferences.contains(prefKeyName) || force) {
            setInt(prefKeyName, defaultValue);
        }
    }

    private void initLongPreference(String prefKeyName, long defaultValue, boolean force) {
        if (!preferences.contains(prefKeyName) || force) {
            setLong(prefKeyName, defaultValue);
        }
    }

    private void initFilePreference(String prefKeyName, File defaultValue, boolean force) {
        if (!preferences.contains(prefKeyName) || force) {
            setFile(prefKeyName, defaultValue);
        }
    }

    private void initStringArrayPreference(String prefKeyName, String[] defaultValue, boolean force) {
        if (!preferences.contains(prefKeyName) || force) {
            setStringArray(prefKeyName, defaultValue);
        }
    }

    private void initSerializable(String prefKeyName, Serializable defaultValue, boolean force) {
        if (!preferences.contains(prefKeyName) || force) {
            setSerializable(prefKeyName, defaultValue);
        }
    }

    private void resetToDefaults(Map<String, Object> map) {
        for (Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof String) {
                setString(entry.getKey(), (String) entry.getValue());
            } else if (entry.getValue() instanceof Integer) {
                setInt(entry.getKey(), (Integer) entry.getValue());
            } else if (entry.getValue() instanceof Long) {
                setLong(entry.getKey(), (Long) entry.getValue());
            } else if (entry.getValue() instanceof Boolean) {
                setBoolean(entry.getKey(), (Boolean) entry.getValue());
            } else if (entry.getValue() instanceof byte[]) {
                setByteArray(entry.getKey(), (byte[]) entry.getValue());
            } else if (entry.getValue() instanceof File) {
                setFile(entry.getKey(), (File) entry.getValue());
            } else if (entry.getValue() instanceof String[]) {
                setStringArray(entry.getKey(), (String[]) entry.getValue());
            } else if (entry.getValue() instanceof Serializable) {
                setSerializable(entry.getKey(), (Serializable) entry.getValue());
            }
        }
    }
}
