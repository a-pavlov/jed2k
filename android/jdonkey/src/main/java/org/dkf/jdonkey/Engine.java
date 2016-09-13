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

package org.dkf.jdonkey;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import org.dkf.jed2k.alert.*;
import org.dkf.jed2k.android.AlertListener;
import org.dkf.jed2k.android.ED2KService;
import org.dkf.jed2k.util.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;

/**
 * @author gubatron
 * @author aldenml
 */
public final class Engine implements AlertListener {
    private static final Logger LOG = LoggerFactory.getLogger(Engine.class);
    private ED2KService service;
    private ServiceConnection connection;
    private Context context;
    private LinkedList<AlertListener> pendingListeners = new LinkedList<>();
    static final ExecutorService threadPool = ThreadPool.newThreadPool("Engine");

    private static Engine instance;

    @Override
    public void onListen(ListenAlert alert) {

    }

    @Override
    public void onSearchResult(SearchResultAlert alert) {

    }

    @Override
    public void onServerConnectionAlert(ServerConnectionAlert alert) {

    }

    @Override
    public void onServerMessage(ServerMessageAlert alert) {

    }

    @Override
    public void onServerStatus(ServerStatusAlert alert) {

    }

    @Override
    public void onServerIdAlert(ServerIdAlert alert) {

    }

    @Override
    public void onServerConnectionClosed(ServerConectionClosed alert) {

    }

    public synchronized static void create(Application context) {
        if (instance != null) {
            return;
        }
        instance = new Engine(context);
    }

    public static Engine instance() {
        if (instance == null) {
            throw new RuntimeException("Engine not created");
        }
        return instance;
    }

    private Engine(Application context) {
        this.context = context;
        startEngineService(context);
    }

    public void startServices() {
        if (service != null) {
            service.startServices();
        }
    }

    public void stopServices(boolean disconnected) {
        if (service != null) {
            service.stopServices();
        }
    }

    public boolean isDisconnected() {
        return false;
    }

    public void shutdown() {
        if (service != null) {
            if (connection != null) {
                try {
                    context.unbindService(connection);
                } catch (IllegalArgumentException e) {
                }
            }

            /*if (receiver != null) {
                try {
                    getApplication().unregisterReceiver(receiver);
                } catch (IllegalArgumentException e) {
                }
            }
            */

            service.shutdown();
        }
    }

    public void setListener(final AlertListener ls) {
        if (service != null) service.addListener(ls);
        else pendingListeners.add(ls);
    }

    public void removeListener(final AlertListener ls) {
        if (service != null) service.removeListener(ls);
        pendingListeners.remove(ls);
    }

    public boolean connectTo(final String serverId, final String host, int port) {
        if (service != null) {
            try {
                service.connectoServer(serverId, host, port);
            } catch(Exception e) {
                Log.e("server connection", e.toString());
                return false;
            }
        }

        return true;
    }

    public void disconnectFrom() {
        if (service != null) {
            try {
                service.disconnectServer();
            } catch(Exception e) {

            }
        }
    }

    public void removeServer(final String serverId) {

    }

    public String getCurrentServerId() {
        if (service != null) return service.getCurrentServerId();
        return "";
    }

    /**
     * @param context This must be the application context, otherwise there will be a leak.
     */
    private void startEngineService(final Context context) {
        Intent i = new Intent();
        i.setClass(context, ED2KService.class);
        context.startService(i);
        context.bindService(i, connection = new ServiceConnection() {
            public void onServiceDisconnected(ComponentName name) {

            }

            public void onServiceConnected(ComponentName name, IBinder service) {
                if (service instanceof ED2KService.ED2KServiceBinder) {
                    LOG.info("Service connected");
                    Engine.this.service = ((ED2KService.ED2KServiceBinder) service).getService();
                    for(final AlertListener ls: pendingListeners) {
                        Engine.this.service.addListener(ls);
                    }
                    LOG.info("bind {} pending listeners", pendingListeners.size());
                    pendingListeners.clear();
                    //registerStatusReceiver(context);
                } else {
                    throw new IllegalArgumentException("IBinder on service connected class is not instance of ED2KService.ED2KServiceBinder");
                }
            }
        }, Context.BIND_AUTO_CREATE);
    }

    public void performSearch(final String query) {
        if (service != null) {
            service.startSearch(0,0,0,0,"","","",0,0,query);
        }
    }

    public ExecutorService getThreadPool() {
        return threadPool;
    }
}
