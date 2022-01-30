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

package org.dkf.jmule;

import android.app.Application;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.File;

/**
 * @author gubatron
 * @author aldenml
 */
public final class  NetworkManager {

    private final Application context;

    private boolean tunnelUp;

    private static NetworkManager instance;

    public synchronized static void create(Application context) {
        if (instance != null) {
            return;
        }
        instance = new NetworkManager(context);
    }

    public static NetworkManager instance() {
        if (instance == null) {
            throw new RuntimeException("NetworkManager not created");
        }
        return instance;
    }

    private NetworkManager(Application context) {
        this.context = context;
    }

    public boolean isInternetDown() {
        return !isDataWIFIUp() && !isDataMobileUp() && !isDataWiMAXUp();
    }

    public boolean isDataUp() {
        // boolean logic trick, since sometimes android reports WIFI and MOBILE up at the same time
        return (isDataWIFIUp() != isDataMobileUp()) || isDataWiMAXUp();
    }

    public boolean isDataMobileUp() {
        ConnectivityManager connectivityManager = getConnectivityManager();
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
    }

    public boolean isDataWIFIUp() {
        ConnectivityManager connectivityManager = getConnectivityManager();
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
    }

    public boolean isDataWiMAXUp() {
        ConnectivityManager connectivityManager = getConnectivityManager();
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);
        return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
    }

    private ConnectivityManager getConnectivityManager() {
        return (ConnectivityManager) context.getSystemService(Application.CONNECTIVITY_SERVICE);
    }

    public boolean isTunnelUp() {
        return tunnelUp;
    }

    // eventually move this to the Platform framework
    public void detectTunnel() {
        tunnelUp = isValidInterfaceName("tun0");
    }

    private static boolean isValidInterfaceName(String interfaceName) {
        try {
            String[] arr = new File("/sys/class/net").list();
            if (arr == null) {
                return false;
            }
            for (int i = 0; i < arr.length; i++) {
                String validName = arr[i];
                if (interfaceName.equals(validName)) {
                    return true;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return false;
    }
}
