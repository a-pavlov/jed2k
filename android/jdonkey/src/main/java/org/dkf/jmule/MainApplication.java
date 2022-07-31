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
import com.squareup.leakcanary.LeakCanary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author gubatron
 * @author aldenml
 */
public class MainApplication extends Application {
    private static final Logger LOG = LoggerFactory.getLogger(MainApplication.class);

    @Override
    public void onTerminate() {
        super.onTerminate();
        LOG.info("application terminated");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }

        LeakCanary.install(this);

        try {

            Platforms.set(new AndroidPlatform(this));
            ConfigurationManager.create(this);
            NetworkManager.create(this);
            Engine.instance().onApplicationCreate(this);
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialized main components", e);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
}
