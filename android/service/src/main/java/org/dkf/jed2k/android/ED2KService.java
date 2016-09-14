package org.dkf.jed2k.android;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ED2KService extends Service {
    public final static int ED2K_STATUS_NOTIFICATION = 0x7ada5021;
    private final Logger log = LoggerFactory.getLogger(ED2KService.class);

    private Binder binder;

    /**
     * session settings, currently with default parameters
     */
    private Settings settings  = new Settings();

    /**
     * main ed2k session
     */
    private Session session;

    private Set<Hash> localHashes = new HashSet<>();

    /**
     * dedicated thread executor for scan session's alerts
     */
    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    private final String NOTIFICATION_INTENT_OPEN = "org.dkf.jed2k.android.INTENT_OPEN";

    private final String NOTIFICATION_INTENT_CLOSE = "org.dkf.jed2k.android.INTENT_CLOSE";

    private AtomicBoolean listening = new AtomicBoolean(false);

    private ScheduledFuture scheduledFuture;

    private ResumeDataLoader rdLoader = new ResumeDataLoader();

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
        settings.listenPort = 5000;
        session = new Session(settings);
        session.start();
        alertsLoop();
        rdLoader.execute();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log.info("ED2K service start command");
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();

        if (intent == null) {
            return 0;
        }

        String action = intent.getAction();
        if (action != null ) {

            if (action.equals(NOTIFICATION_INTENT_CLOSE)) {
                if (mNotificationManager != null)
                    mNotificationManager.cancel(NOTIFICATION_ID);
            } else if (action.equals(NOTIFICATION_INTENT_OPEN)) {

            }
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
        scheduledExecutorService.shutdown();
    }

    synchronized public void addListener(AlertListener listener) {
        listeners.add(listener);
    }

    synchronized public void removeListener(AlertListener listener) {
        listeners.remove(listener);
    }

    public synchronized boolean containsHash(final Hash h) {
        return localHashes.contains(h);
    }

    synchronized public void processAlert(Alert a) {
        log.debug("ED2KService service alive");
        if (a instanceof ListenAlert) {
            for(final AlertListener ls: listeners) ls.onListen((ListenAlert)a);
        } else if (a instanceof SearchResultAlert) {
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
        else if (a instanceof TransferAddedAlert) {
            localHashes.add(((TransferAddedAlert) a).hash);
        }
        else if (a instanceof TransferRemovedAlert) {
            localHashes.remove(((TransferAddedAlert) a).hash);
        }
        else if (a instanceof TransferResumeDataAlert) {
            final TransferResumeDataAlert alert = (TransferResumeDataAlert)a;
            FileOutputStream stream = null;
            try {
                stream = openFileOutput("rd_" + alert.hash.toString(), MODE_PRIVATE);
                ByteBuffer bb = ByteBuffer.allocate(alert.trd.bytesCount());
                alert.trd.put(bb);
                bb.flip();
                stream.write(bb.array(), 0, bb.limit());
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
        else {
            log.debug("alert {}", a);
        }
    }

    private void alertsLoop() {
        Log.d("ED2KService", "alertsLoop");
        assert(session != null);
        scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Alert a = session.popAlert();
                while(a != null) {
                    processAlert(a);
                    a = session.popAlert();
                }
            }
        },  100, 2000, TimeUnit.MILLISECONDS);
    }

    /**
     * scan files directory of program and attempt to load each resume data files
     */
    private class ResumeDataLoader extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            File fd = getFilesDir();
            File[] files = fd.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return (pathname.getName().startsWith("rd_"));
                }
            });

            if (files == null) {
                log.info("have no resume data files");
                return null;
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
                    istream = openFileInput(f.getName());
                    istream.read(buffer.array(), 0, buffer.capacity());
                    buffer.flip();
                    AddTransferParams atp = new AddTransferParams();
                    atp.get(buffer);
                    if (session != null) {
                        session.addTransfer(atp);
                    }
                }
                catch(FileNotFoundException e) {
                    log.error("load resume data file not found {} error {}", f.getName(), e);
                }
                catch(IOException e) {
                    log.error("load resume data {} i/o error {}", f.getName(), e);
                }
                catch(JED2KException e) {
                    log.error("load resume data {} serialization error {}", f.getName(), e);
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

            return null;
        }
    }

    private void buildNotification(final String fileName, final String fileHash, Bitmap artImage) {
        /**
         * Intents
         */
        Intent intentOpen = new Intent(NOTIFICATION_INTENT_OPEN);
        Intent intentClose = new Intent(NOTIFICATION_INTENT_CLOSE);

        /**
         * Pending intents
         */
        PendingIntent openPending = PendingIntent.getService(this, 0, intentOpen, 0);
        PendingIntent closePending = PendingIntent.getService(this, 0, intentClose, 0);

        /**
         * Remote view for normal view
         */

        RemoteViews mNotificationTemplate = new RemoteViews(this.getPackageName(), R.layout.notification);
        Notification.Builder notificationBuilder = new Notification.Builder(this);

        /**
         * set small notification texts and image
         */
        if (artImage == null)
            artImage = BitmapFactory.decodeResource(getResources(), R.drawable.default_art);

        mNotificationTemplate.setTextViewText(R.id.notification_line_one, fileName);
        mNotificationTemplate.setTextViewText(R.id.notification_line_two, fileHash);
        mNotificationTemplate.setImageViewResource(R.id.notification_play, R.drawable.btn_playback_pause /* : R.drawable.btn_playback_play*/);
        mNotificationTemplate.setImageViewBitmap(R.id.notification_image, artImage);

        /**
         * OnClickPending intent for collapsed notification
         */
        mNotificationTemplate.setOnClickPendingIntent(R.id.notification_collapse, openPending);
        mNotificationTemplate.setOnClickPendingIntent(R.id.notification_play, closePending);

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
        notification.flags = Notification.FLAG_ONGOING_EVENT;

        /**
         * Expanded notification
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

            RemoteViews mExpandedView = new RemoteViews(this.getPackageName(), R.layout.notification_expanded);

            mExpandedView.setTextViewText(R.id.notification_line_one, fileName);
            mExpandedView.setTextViewText(R.id.notification_line_two, fileHash);
            mExpandedView.setImageViewResource(R.id.notification_expanded_play, R.drawable.btn_playback_pause/* : R.drawable.btn_playback_play*/);
            mExpandedView.setImageViewBitmap(R.id.notification_image, artImage);

            mExpandedView.setOnClickPendingIntent(R.id.notification_collapse, openPending);
            mExpandedView.setOnClickPendingIntent(R.id.notification_expanded_play, closePending);
            notification.bigContentView = mExpandedView;
        }

        if (mNotificationManager != null)
            mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    public void updateNotification(final String fileName, final String fileHash, int smallImage, int artImage) {
        updateNotification(fileName, fileHash, smallImage, BitmapFactory.decodeResource(getResources(), artImage));
    }

    public void updateNotification(final String fileName, final String fileHash, int smallImage, Bitmap artImage) {
        this.smallImage = smallImage;
        buildNotification(fileName, fileHash, artImage);
    }

    // only for testing
    public final boolean isListening() {
        return listening.get();
    }

    public void connectoServer(final String serverId, final String host, final int port) {
        Log.i("server connection", "Connect to " + host);
        session.connectoTo(serverId, host, port);
    }

    public void disconnectServer() {
        Log.i("server connection", "disconnect from");
        session.disconnectFrom();
    }

    public String getCurrentServerId() {
        return session.getConnectedServerId();
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
        assert(session != null);

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
        session.searchMore();
    }

    /**
     * setup session preferences
     * @param settings new session config
     */
    public void configureSession(final Settings settings) {
        this.settings = settings;
        session.configureSession(settings);
    }

    public void startServices() {

    }

    public void stopServices() {

    }

    public void shutdown(){
        stopForeground(true);
        stopSelf(-1);
    }

    public TransferHandle addTransfer(final Hash hash, final long fileSize, final String filePath) throws JED2KException {
        Log.i("ED2KService", "start transfer " + hash.toString() + " file " + filePath + " size " + fileSize);
        TransferHandle handle = session.addTransfer(hash, fileSize, filePath);
        if (handle.isValid()) {
            Log.i("ED2KService", "handle is valid");
        }

        return handle;
    }

    public List<TransferHandle> getTransfers() {
        return session.getTransfers();
    }
}
