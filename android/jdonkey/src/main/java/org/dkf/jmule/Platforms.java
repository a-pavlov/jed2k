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

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author gubatron
 * @author aldenml
 */
public final class Platforms {
    private static final Logger LOG = LoggerFactory.getLogger(Platforms.class);

    private static AndroidPlatform platform;

    private Platforms() {
    }

    public static AndroidPlatform get() {
        if (platform == null) {
            throw new IllegalStateException("Platform can't be null");
        }
        return platform;
    }

    public static void set(AndroidPlatform p) {
        if (p == null) {
            throw new IllegalArgumentException("Platform can't be set to null");
        }
        platform = p;
    }

    /**
     * Shortcut to current platform file system.
     *
     * @return
     */
    public static FileSystem fileSystem() {
        return get().fileSystem();
    }

    /**
     * Shortcut to current platform application settings.
     *
     * @return
     */
    public static AndroidSettings appSettings() {
        return get().appSettings();
    }

    public static File data() {
        File f = get().systemPaths().data();
        LOG.warn("PLATFORM FILE " + f.getAbsolutePath());
        return f;
    }
}
