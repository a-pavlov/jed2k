package org.dkf.jed2k.android;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import org.dkf.jed2k.*;
import org.dkf.jed2k.alert.*;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Hash;
import org.dkf.jed2k.protocol.server.search.SearchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ED2KService extends Service {
    public final static int ED2K_STATUS_NOTIFICATION = 0x7ada5021;
    private final Logger log = LoggerFactory.getLogger(ED2KService.class);

    public static final String ACTION_SHOW_TRANSFERS = "org.dkf.jmule.android.ACTION_SHOW_TRANSFERS";
    public static final String EXTRA_DOWNLOAD_COMPLETE_NOTIFICATION = "org.dkf.jmule.EXTRA_DOWNLOAD_COMPLETE_NOTIFICATION";

    private Binder binder;

    /**
     * run notifications in ui thread
     */
    Handler notificationHandler = new Handler();

    /**
     * session settings, currently with default parameters
     */
    private Settings settings  = new Settings();

    /**
     * main ed2k session
     */
    private Session session;

    /**
     * cached hashes of transfers to provide informaion about hashes we have
     */
    private Map<Hash, Integer> localHashes = Collections.synchronizedMap(new HashMap<Hash, Integer>());

    /**
     * this map contains last time when i/o error occurred on transfer
     * uses to avoid multiple notifications about i/o errors
     */
    private Map<Hash, Long> transfersIOErrorsOrder = new HashMap<>();

    /**
     * dedicated thread executor for scan session's alerts and some other actions like resume data loading
     */
    ScheduledExecutorService scheduledExecutorService;

    /**
     * scheduled task which scan session's alerts container and produce result for listeners
     */
    private ScheduledFuture scheduledFuture;

    private boolean startingInProgress = false;
    private boolean stoppingInProgress = false;

    /**
     * trivial listener
     */
    private LinkedList<AlertListener> listeners = new LinkedList<>();

    /**
     * Notification ID
     */
    private static final int NOTIFICATION_ID = 001;

    private int smallImage = R.drawable.default_art;

    /**
     * Notification manager
     */
    private NotificationManager mNotificationManager;

    public ED2KService() {
        binder = new ED2KServiceBinder();
    }

    public class ED2KServiceBinder extends Binder {
        public ED2KService getService() {
            return ED2KService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        log.info("ED2K service creating....");
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        settings.serverPingTimeout = 20;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();

        if (intent == null) {
            return 0;
        }

        log.info("ED2K service started by this intent: {} flags {} startId {}", intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        log.info("ED2K service destructing...");

        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();

        if (session != null) {
            session.abort();
            try {
                session.join();
                log.info("ED2K service session aborted");
            } catch (InterruptedException e) {
                log.error("wait session interrupted error {}", e);
            }
        } else {
            log.debug("session is not exist yet");
        }

        // stop alerts processing
        // need additional code to guarantee all alerts were processed
        if (scheduledExecutorService != null) scheduledExecutorService.shutdown();
    }

    void startSession() {
        if (session != null) return;
        log.info("starting session....");
        startingInProgress = true;
        session = new Session(settings);
        session.start();
        startBackgroundOperations();
        startingInProgress = false;
        log.info("session started!");
    }

    void stopSession() {
        if (session != null) {
            log.info("stopping session....");
            stoppingInProgress = true;
            session.saveResumeData();
            session.abort();

            try {
                session.join();
            } catch (InterruptedException e) {

            } finally {

                try {
                    scheduledExecutorService.shutdown();
                    scheduledExecutorService.awaitTermination(4, TimeUnit.SECONDS);

                    // catch all remain events and process save resume data
                    Alert a = session.popAlert();
                    while(a != null) {
                        if (a instanceof TransferResumeDataAlert) {
                            saveResumeData((TransferResumeDataAlert)a);
                        }
                        a = session.popAlert();
                    }
                } catch(InterruptedException e) {
                    log.error("alert loop await interrupted {}", e);
                }

                session = null;
                scheduledExecutorService = null;
            }

            stoppingInProgress = false;
            log.info("session stopped!");
        }
    }

    public boolean isStarting() {
        return startingInProgress;
    }

    public boolean isStopping() {
        return stoppingInProgress;
    }

    public boolean isStarted() {
        return session != null && !isStarting() && !isStopping();
    }

    public boolean isStopped() {
        return session == null && !isStarting() && !isStopping();
    }

    synchronized public void addListener(AlertListener listener) {
        listeners.add(listener);
    }

    synchronized public void removeListener(AlertListener listener) {
        listeners.remove(listener);
    }

    public boolean containsHash(final Hash h) {
        return localHashes.containsKey(h);
    }

    /**
     * remove resume data file for transfer
     * @param hash transfer's hash
     */
    private void removeResumeDataFile(final Hash hash) {
        deleteFile("rd_" + hash.toString());
    }

    private void createTransferNotification(final String title, final String extra, final Hash hash) {
        TransferHandle handle = session.findTransfer(hash);
        if (handle.isValid()) {
            buildNotification(title, handle.getFilePath().getName(), extra);
        }
    }

    private void saveResumeData(final TransferResumeDataAlert a) {
        final TransferResumeDataAlert alert = (TransferResumeDataAlert)a;
        FileOutputStream stream = null;
        try {
            stream = openFileOutput("rd_" + alert.hash.toString(), MODE_PRIVATE);
            ByteBuffer bb = ByteBuffer.allocate(alert.trd.bytesCount());
            alert.trd.put(bb);
            bb.flip();
            stream.write(bb.array(), 0, bb.limit());
            log.info("saved resume data {} size {}", alert.hash.toString(), alert.trd.bytesCount());
        } catch(FileNotFoundException e) {
            log.error("save resume data {} failed {}", alert.hash, e);
        } catch(IOException e) {
            log.error("save resume data write {} failed {}", alert.hash, e);
        }
        catch(JED2KException e) {
            log.error("save resume data serialization {} failed {}", alert.hash, e);
        }
        finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch(IOException e) {
                    // just ignore
                    log.error("save resume data close stream failed {}", e);
                }
            }
        }
    }

    public void processAlert(final Alert a) {
        if (a instanceof ListenAlert) {
            for(final AlertListener ls: listeners) ls.onListen((ListenAlert)a);
        } else if (a instanceof SearchResultAlert) {
            log.info("search result received");
            for(final AlertListener ls: listeners)  ls.onSearchResult((SearchResultAlert)a);
        }
        else if (a instanceof ServerMessageAlert) {
            for(final AlertListener ls: listeners) ls.onServerMessage((ServerMessageAlert)a);
        }
        else if (a instanceof ServerStatusAlert) {
            for(final AlertListener ls: listeners) ls.onServerStatus((ServerStatusAlert)a);
        }
        else if (a instanceof ServerConectionClosed) {
            for(final AlertListener ls: listeners) ls.onServerConnectionClosed((ServerConectionClosed)a);
        }
        else if (a instanceof ServerIdAlert) {
            for(final AlertListener ls: listeners) ls.onServerIdAlert((ServerIdAlert) a);
        }
        else if (a instanceof ServerConnectionAlert) {
            for(final AlertListener ls: listeners) ls.onServerConnectionAlert((ServerConnectionAlert)a);
        }
        else if (a instanceof TransferResumedAlert) {
            for(final AlertListener ls: listeners) ls.onTransferResumed((TransferResumedAlert) a);
        }
        else if (a instanceof TransferPausedAlert) {
            for(final AlertListener ls: listeners) ls.onTransferPaused((TransferPausedAlert) a);
        }
        else if (a instanceof TransferAddedAlert) {
            localHashes.put(((TransferAddedAlert) a).hash, 0);
            log.info("new transfer added {} save resume data now", ((TransferAddedAlert) a).hash);
            session.saveResumeData();
        }
        else if (a instanceof TransferRemovedAlert) {
            log.info("transfer removed {}", ((TransferRemovedAlert) a).hash);
            localHashes.remove(((TransferRemovedAlert) a).hash);
            removeResumeDataFile(((TransferRemovedAlert) a).hash);
        }
        else if (a instanceof TransferResumeDataAlert) {
            saveResumeData((TransferResumeDataAlert)a);
        }
        else if (a instanceof TransferFinishedAlert) {
            log.info("transfer finished {} save resume data", ((TransferFinishedAlert) a).hash);
            session.saveResumeData();
            notificationHandler.post(new Runnable() {
                @Override
                public void run() {
                    createTransferNotification(getResources().getString(R.string.transfer_finished), EXTRA_DOWNLOAD_COMPLETE_NOTIFICATION, ((TransferFinishedAlert) a).hash);
                }
            });
        }
        else if (a instanceof TransferDiskIOErrorAlert) {
            TransferDiskIOErrorAlert errorAlert = (TransferDiskIOErrorAlert)a;
            log.error("disk i/o error: {}", errorAlert.ec);
            long lastIOErrorTime = 0;
            if (transfersIOErrorsOrder.containsKey(errorAlert.hash)) {
                lastIOErrorTime = transfersIOErrorsOrder.get(errorAlert.hash);
            }

            transfersIOErrorsOrder.put(errorAlert.hash, errorAlert.getCreationTime());

            // dispatch alert if no i/o errors on this transfer in last 10 seconds
            if (errorAlert.getCreationTime() - lastIOErrorTime > 10*1000) {
                for(final AlertListener ls: listeners) ls.onTransferIOError(errorAlert);
                notificationHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        createTransferNotification(getResources().getString(R.string.transfer_io_error), "", ((TransferDiskIOErrorAlert) a).hash);
                    }
                });
            }
        }
        else {
            log.debug("received unhandled alert {}", a);
        }
    }

    private void startBackgroundOperations() {
        assert(session != null);
        assert(scheduledExecutorService == null);
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                Alert a = session.popAlert();
                while(a != null) {
                    processAlert(a);
                    a = session.popAlert();
                }
            }
        },  100, 2000, TimeUnit.MILLISECONDS);

        // save resume data every 200 seconds
        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                session.saveResumeData();
            }
        }, 60, 200, TimeUnit.SECONDS);

        scheduledExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                log.info("load resume data");
                File fd = getFilesDir();
                File[] files = fd.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return (pathname.getName().startsWith("rd_"));
                    }
                });

                if (files == null) {
                    log.info("have no resume data files");
                    return;
                }

                for(final File f: files) {
                    long fileSize = f.length();
                    if (fileSize > Constants.BLOCK_SIZE_INT) {
                        log.warn("resume data file {} has too large size {}, skip it", f.getName(), fileSize);
                        continue;
                    }

                    ByteBuffer buffer = ByteBuffer.allocate((int)fileSize);
                    FileInputStream istream = null;
                    try {
                        log.info("load resume data {} size {}", f.getName(), fileSize);
                        if (session == null) {
                            log.info("session null");
                        } else {
                            log.info("session not null");
                        }
                        istream = openFileInput(f.getName());
                        istream.read(buffer.array(), 0, buffer.capacity());
                        // do not flip buffer!
                        AddTransferParams atp = new AddTransferParams();
                        atp.get(buffer);
                        if (session != null) {
                            TransferHandle handle = session.addTransfer(atp);
                            if (handle.isValid()) {
                                log.info("transfer {} is valid", handle.getHash());
                            } else {
                                log.info("transfer invalid");
                            }
                        }
                    }
                    catch(FileNotFoundException e) {
                        log.error("load resume data file not found {} error {}", f.getName(), e);
                    }
                    catch(IOException e) {
                        log.error("load resume data {} i/o error {}", f.getName(), e);
                    }
                    catch(JED2KException e) {
                        log.error("load resume data {} add transfer error {}", f.getName(), e);
                    }
                    finally {
                        if (istream != null) {
                            try {
                                istream.close();
                            } catch(Exception e) {
                                log.error("load resume data {} close stream error {}", f.getName(), e);
                            }
                        }
                    }

                }
            }
        });
    }

    private void buildNotification(final String title, final String summary, final String extra) {
        Intent intentShowTransfers = new Intent(ACTION_SHOW_TRANSFERS);
        if (!extra.isEmpty()) intentShowTransfers.putExtra(extra, true);

        /**
         * Pending intents
         */
        PendingIntent openPending = PendingIntent.getActivity(getApplicationContext(), 0, intentShowTransfers, 0);

        /**
         * Remote view for normal view
         */

        Bitmap art = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);

        RemoteViews mNotificationTemplate = new RemoteViews(this.getPackageName(), R.layout.notification);
        Notification.Builder notificationBuilder = new Notification.Builder(this);

        mNotificationTemplate.setTextViewText(R.id.notification_line_one, title);
        mNotificationTemplate.setTextViewText(R.id.notification_line_two, summary);
        //mNotificationTemplate.setImageViewResource(R.id.notification_play, R.drawable.btn_playback_pause /* : R.drawable.btn_playback_play*/);
        mNotificationTemplate.setImageViewBitmap(R.id.notification_image, art);

        /**
         * OnClickPending intent for collapsed notification
         */
        mNotificationTemplate.setOnClickPendingIntent(R.id.notification_collapse, openPending);

        /**
         * Create notification instance
         */
        Notification notification = notificationBuilder
                .setSmallIcon(smallImage)
                .setContentIntent(openPending)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setContent(mNotificationTemplate)
                .setUsesChronometer(true)
                .build();
        //notification.flags = Notification.FLAG_ONGOING_EVENT;

/*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

            RemoteViews mExpandedView = new RemoteViews(this.getPackageName(), R.layout.notification_expanded);

            mExpandedView.setTextViewText(R.id.notification_line_one, title);
            mExpandedView.setTextViewText(R.id.notification_line_two, summary);
            mExpandedView.setImageViewResource(R.id.notification_expanded_play, R.drawable.btn_playback_pause : R.drawable.btn_playback_play);
            mExpandedView.setImageViewBitmap(R.id.notification_image, );

            mExpandedView.setOnClickPendingIntent(R.id.notification_collapse, openPending);
            mExpandedView.setOnClickPendingIntent(R.id.notification_expanded_play, closePending);
            notification.bigContentView = mExpandedView;
        }
*/
        if (mNotificationManager != null)
            mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    public void connectoServer(final String serverId, final String host, final int port) {
        if (session != null) session.connectoTo(serverId, host, port);
    }

    public void disconnectServer() {
         if (session != null) session.disconnectFrom();
    }

    public String getCurrentServerId() {
        if (session != null) return session.getConnectedServerId();
        return "";
    }

    /**
     *  async search request
     * @param minSize minimal file size in bytes
     * @param maxSize max file size in bytes
     * @param sourcesCount min sources count
     * @param completeSourcesCount min complete sources count
     * @param fileType file type as string
     * @param fileExtension file extension
     * @param codec media codec
     * @param mediaLength media length
     * @param mediaBitrate media bitrate
     * @param phrase search phrase
     */
    public void startSearch(long minSize,
                            long maxSize,
                            int sourcesCount,
                            int completeSourcesCount,
                            final String fileType,
                            final String fileExtension,
                            final String codec,
                            int mediaLength,
                            int mediaBitrate,
                            final String phrase) {
        if (session == null) return;

        try {
            session.search(SearchRequest.makeRequest(minSize, maxSize, sourcesCount, completeSourcesCount, fileType, fileExtension, codec, mediaLength, mediaBitrate, phrase));
        } catch(JED2KException e) {
            log.error("search request error {}", e);
        }
    }

    /**
     * search more results, run only if search result has flag more results
     */
    public void searchMore() {
        if (session != null) session.searchMore();
    }

    public void startServices() {
        startSession();
    }

    public void stopServices() {
        stopSession();
    }

    public void shutdown(){
        stopServices();
        stopForeground(true);
        stopSelf(-1);
    }

    public TransferHandle addTransfer(final Hash hash, final long fileSize, final String filePath) throws JED2KException {
        if(session != null) {
            Log.i("ED2KService", "start transfer " + hash.toString() + " file " + filePath + " size " + fileSize);
            TransferHandle handle = session.addTransfer(hash, fileSize, filePath);
            if (handle.isValid()) {
                Log.i("ED2KService", "handle is valid");
            }

            return handle;
        }

        return null;
    }

    public List<TransferHandle> getTransfers() {
        if (session != null) {
            return session.getTransfers();
        }

        return new ArrayList<TransferHandle>();
    }

    public void removeTransfer(final Hash h, final boolean removeFile) {
        if (session != null) {
            session.removeTransfer(h, removeFile);
        }
    }

    public void configureSession() {
        if (session != null) {
            session.configureSession(settings);
        }
    }

    public void setNickname(final String name) {
        settings.clientName = name;
    }

    public void setListenPort(int port) {
        settings.listenPort = port;
    }

    public void setMaxPeerListSize(int maxSize) {
        settings.maxPeerListSize = maxSize;
    }

    public Pair<Long, Long> getDownloadUploadRate() {
        if (session != null) {
            return session.getDownloadUploadRate();
        }

        return Pair.make(0l, 0l);
    }
}
