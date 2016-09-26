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

import android.app.Application;
import android.view.ViewConfiguration;
import org.apache.commons.io.FileUtils;
import org.dkf.jmule.core.AndroidPlatform;
import org.dkf.jmule.core.ConfigurationManager;
import org.dkf.jmule.core.NetworkManager;
import org.dkf.jmule.core.Platforms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Field;

/**
 * @author gubatron
 * @author aldenml
 */
public class MainApplication extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(MainApplication.class);

    @Override
    public void onCreate() {
        super.onCreate();

        try {

            Platforms.set(new AndroidPlatform(this));
            ConfigurationManager.create(this);
            NetworkManager.create(this);
            Engine.create(this);
            /*
            PlayStore.getInstance().initialize(this); // as early as possible

            ignoreHardwareMenu();
            installHttpCache();

            ConfigurationManager.create(this);

            Platforms.set(new AndroidPlatform(this));

            NetworkManager.create(this);
            Librarian.create(this);
            Engine.create(this);

            ImageLoader.getInstance(this);
            CrawlPagedWebSearchPerformer.setCache(new DiskCrawlCache(this));
            CrawlPagedWebSearchPerformer.setMagnetDownloader(null); // this effectively turn off magnet downloads

            LocalSearchEngine.create();

            cleanTemp();

            Librarian.instance().syncMediaStore();
            */
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialized main components", e);
        }
    }

    @Override
    public void onLowMemory() {
        //ImageCache.getInstance(this).evictAll();
        //ImageLoader.getInstance(this).clear();
        super.onLowMemory();
    }

    private void ignoreHardwareMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field f = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (f != null) {
                f.setAccessible(true);
                f.setBoolean(config, false);
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private void installHttpCache() {
        /*
        try {
            HttpResponseCache.install(this);
        } catch (IOException e) {
            LOG.error("Unable to install global http cache", e);
        }
        */
    }

    private void cleanTemp() {
        try {
            File tmp = Platforms.get().systemPaths().temp();
            if (tmp.exists()) {
                FileUtils.cleanDirectory(tmp);
            }
        } catch (Exception e) {
            LOG.error("Error during setup of temp directory", e);
        }
    }
}
