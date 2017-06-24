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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import org.dkf.jed2k.EMuleLink;
import org.dkf.jed2k.Pair;
import org.dkf.jed2k.TransferHandle;
import org.dkf.jed2k.alert.*;
import org.dkf.jed2k.android.AlertListener;
import org.dkf.jed2k.android.ConfigurationManager;
import org.dkf.jed2k.android.Constants;
import org.dkf.jed2k.android.ED2KService;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Hash;
import org.dkf.jed2k.protocol.kad.KadId;
import org.dkf.jed2k.protocol.kad.KadNodesDat;
import org.dkf.jed2k.util.ThreadPool;
import org.dkf.jmule.transfers.ED2KTransfer;
import org.dkf.jmule.transfers.Transfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @author gubatron
 * @author aldenml
 */
public final class Engine implements AlertListener {
    private static final Logger log = LoggerFactory.getLogger(Engine.class);
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

    @Override
    public void onTransferAdded(TransferAddedAlert alert) {

    }

    @Override
    public void onTransferRemoved(TransferRemovedAlert alert) {

    }

    @Override
    public void onTransferPaused(TransferPausedAlert alert) {

    }

    @Override
    public void onTransferResumed(TransferResumedAlert alert) {

    }

    @Override
    public void onTransferIOError(TransferDiskIOErrorAlert alert) {

    }

    @Override
    public void onPortMapAlert(PortMapAlert alert) {

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
            log.info("last server {}", ConfigurationManager.instance().getLastServerConnectionId());
            if (ConfigurationManager.instance().connectoToServerOnRestart() && !ConfigurationManager.instance().getLastServerConnectionId().isEmpty()) {
                log.info("connect to last server {}", ConfigurationManager.instance().getLastServerConnectionId());
                connectTo(
                        ConfigurationManager.instance().getLastServerConnectionId(),
                        ConfigurationManager.instance().getLastServerConnectionHost(),
                        ConfigurationManager.instance().getLastServerConnectionPort());
            }
        }
    }

    public void stopServices(boolean disconnected) {
        if (service != null) {
            service.stopServices();
        }
    }

    public boolean isStopping() {
        return service != null? service.isStopping():false;
    }

    public boolean isStarting() {
        return service != null?service.isStarting():false;
    }

    public boolean isStopped() {
        return service != null ? service.isStopped():true;
    }

    public boolean isStarted() {
        return service != null? service.isStarted():false;
    }

    public void shutdown() {
        if (service != null) {
            log.info("shutdown service");
            if (connection != null) {
                try {
                    log.info("unbind connection");
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
                ConfigurationManager.instance().setLastServerConnection(serverId, host, port);
                service.connectoServer(serverId, host, port);
            } catch(Exception e) {
                log.error("server connection failed {}", e);
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
        log.info("start engine service");
        Intent i = new Intent();
        i.setClass(context, ED2KService.class);
        context.startService(i);
        context.bindService(i, connection = new ServiceConnection() {

            public void onServiceDisconnected(ComponentName name) {
                log.info("service disconnected {}", name);
                Engine.this.service = null;
            }

            public void onServiceConnected(ComponentName name, IBinder service) {
                if (service instanceof ED2KService.ED2KServiceBinder) {
                    log.info("service connected {}", name);
                    Engine.this.service = ((ED2KService.ED2KServiceBinder) service).getService();
                    for(final AlertListener ls: pendingListeners) {
                        Engine.this.service.addListener(ls);
                    }

                    log.info("bind {} pending listeners", pendingListeners.size());
                    pendingListeners.clear();

                    // sync properties here
                    setListenPort((int) ConfigurationManager.instance().getLong(Constants.PREF_KEY_LISTEN_PORT));
                    setMaxPeersCount((int)ConfigurationManager.instance().getLong(Constants.PREF_KEY_TRANSFER_MAX_TOTAL_CONNECTIONS));
                    setNickname(ConfigurationManager.instance().getString(Constants.PREF_KEY_NICKNAME));
                    setVibrateOnDownloadCompleted(ConfigurationManager.instance().vibrateOnFinishedDownload());
                    setPermanentNotification(ConfigurationManager.instance().getBoolean(Constants.PREF_KEY_GUI_ENABLE_PERMANENT_STATUS_NOTIFICATION));

                    // migrate old versions which have no saved user agent hash
                    String userAgent = ConfigurationManager.instance().getString(Constants.PREF_KEY_USER_AGENT);
                    if (userAgent == null || userAgent.isEmpty()) {
                        userAgent = Hash.random(true).toString();
                        ConfigurationManager.instance().setString(Constants.PREF_KEY_USER_AGENT, userAgent);
                        log.info("previous user agent was not found, generate new {}", userAgent);
                    }

                    log.info("user agent {}", userAgent);
                    setUserAgent(userAgent);

                    // KAD id section
                    String kadId = ConfigurationManager.instance().getString(Constants.PREF_KEY_KAD_ID);
                    if (kadId == null || kadId.isEmpty()) {
                        kadId = new KadId(Hash.random(true)).toString();
                        ConfigurationManager.instance().setString(Constants.PREF_KEY_KAD_ID, kadId);
                        log.info("previous kad id was not found, generate new {}", kadId);
                    }

                    log.info("kad id {}", kadId);
                    setKadId(kadId);

                    configureServices();

                    if (ConfigurationManager.instance().getBoolean(Constants.PREF_KEY_AUTO_START_SERVICE) && isStopped()) {
                        startServices();
                    }

                    forwardPorts(ConfigurationManager.instance().getBoolean(Constants.PREF_KEY_FORWARD_PORTS));
                    useDht(ConfigurationManager.instance().getBoolean(Constants.PREF_KEY_CONNECT_DHT));

                    //registerStatusReceiver(context);
                } else {
                    throw new IllegalArgumentException("IBinder on service connected class is not instance of ED2KService.ED2KServiceBinder");
                }
            }
        }, Context.BIND_AUTO_CREATE);
    }

    public void performSearch(
            long minSize,
            long maxSize,
            int sourcesCount,
            int completeSourcesCount,
            final String fileType,
            final String fileExtension,
            final String codec,
            int mediaLength,
            int mediaBitrate,
            final String query) {
        if (service != null) {
            service.startSearch(minSize,maxSize,sourcesCount,completeSourcesCount,fileType,fileExtension,codec,mediaLength,mediaBitrate,query);
        }
    }

    public void performSearchDhtKeyword(final String keyword
            , final long minSize
            , final long maxSize
            , final int sources
            , final int completeSources) {
        if (service != null) service.startSearchDhtKeyword(keyword, minSize, maxSize, sources, completeSources);
    }

    public void performSearchMore() {
        if (service != null) service.searchMore();
    }

    public boolean hasTransfer(final Hash h) {
        if (service != null) return service.containsHash(h);
        return false;
    }

    /// TODO - (apavlov) fix this methods startDownload and startDownload from link - make them in one fashion
    public Transfer startDownload(final Hash hash, long size, final File file) throws JED2KException {
        if (service != null) return new ED2KTransfer(service.addTransfer(hash, size, file));
        return null;
    }

    public Transfer downloadLink(final EMuleLink link) {
        try {
            return new ED2KTransfer(service.addTransfer(link.getHash()
                    , link.getNumberValue()
                    , new File(ConfigurationManager.instance().getStoragePath(), link.getStringValue())));
        } catch(JED2KException e) {
            log.error("download link error {}", e);
        } catch(Exception e) {
            log.error("download link error {}", e.toString());
        }

        return null;
    }

    public Transfer startDownload(final String slink) {
        try {
            if (service != null) {
                EMuleLink link = EMuleLink.fromString(slink);
                if (link.getType().equals(EMuleLink.LinkType.FILE)) {
                    return new ED2KTransfer(service.addTransfer(link.getHash()
                            , link.getNumberValue()
                            , new File(ConfigurationManager.instance().getStoragePath(), link.getStringValue())));
                } else {
                    // message to userv link is incorrect type
                }
            }
        } catch(JED2KException e) {
            log.error("load link error {}", e);
        } catch(Exception e) {
            log.error("load link error {}", e.toString());
        }

        return null;
    }

    public List<Transfer> getTransfers() {
        List<Transfer> res = new ArrayList<>();

        if (service != null) {
            List<TransferHandle> handles = service.getTransfers();
            for (final TransferHandle h : handles) {
                if (h.isValid()) res.add(new ED2KTransfer(h));
            }
        }

        return res;
    }

    public void removeTransfer(Hash h, boolean removeFile) {
        if (service != null) service.removeTransfer(h, removeFile);
    }

    public ExecutorService getThreadPool() {
        return threadPool;
    }

    public void setNickname(final String name) { if (service != null) service.setNickname(name); }
    public void setVibrateOnDownloadCompleted(boolean vibrate) { if (service != null) service.setVibrateOnDownloadCompleted(vibrate); }
    public void setListenPort(int port) { if (service != null) service.setListenPort(port); }
    public void setMaxPeersCount(int peers) { if (service != null) service.setMaxPeerListSize(peers); }
    public void forwardPorts(boolean forward) { if (service != null) service.setForwardPort(forward);}
    public void useDht(boolean dht) {
        log.info("[engine] use dht {}", dht);
        if (service != null) service.useDht(dht); }

    public void setUserAgent(final String s) {
        assert s != null;
        assert !s.isEmpty();
        if (service != null) service.setUserAgent(Hash.fromString(s));
    }

    public void setKadId(final String s) {
        assert s != null;
        assert !s.isEmpty();
        if (service != null) service.setKadId(KadId.fromString(s));
    }

    public void configureServices() {
        if (service != null) service.configureSession();
    }

    public Pair<Long, Long> getDownloadUploadBandwidth() {
        if (service != null) return service.getDownloadUploadRate();
        return Pair.make(0l, 0l);
    }

    public int getTotalDhtNodes() {
        if (service != null) return service.getTotalDhtNodes();
        return -1;
    }

    public boolean isDhtEnabled() {
        if (service != null) return service.isDhtEnabled();
        return false;
    }

    public boolean addDhtNodes(final KadNodesDat nodes) {
        if (service != null) return service.addNodes(nodes);
        return false;
    }

    public void setPermanentNotification(boolean v) {
        if (service != null) service.setPermanentNotification(v);
    }
}
