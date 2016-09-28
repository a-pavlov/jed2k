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

package org.dkf.jmule.core;

/**
 * @author gubatron
 * @author aldenml
 */
final class AndroidSettings {

    public String string(String key) {
        return ConfigurationManager.instance().getString(key);
    }

    public void string(String key, String value) {
        ConfigurationManager.instance().setString(key, value);
    }

    public int int32(String key) {
        return ConfigurationManager.instance().getInt(key);
    }

    public void int32(String key, int value) {
        ConfigurationManager.instance().setInt(key, value);
    }

    public long int64(String key) {
        return ConfigurationManager.instance().getLong(key);
    }

    public void int64(String key, long value) {
        ConfigurationManager.instance().setLong(key, value);
    }

    public boolean bool(String key) {
        return ConfigurationManager.instance().getBoolean(key);
    }

    public void bool(String key, boolean value) {
        ConfigurationManager.instance().setBoolean(key, value);
    }
}
