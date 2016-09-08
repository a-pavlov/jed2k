package org.dkf.jed2k.android;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import org.dkf.jed2k.R;
import org.dkf.jed2k.Session;
import org.dkf.jed2k.Settings;
import org.dkf.jed2k.alert.*;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.server.search.SearchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
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

    /**
     * dedicated thread executor for scan session's alerts
     */
    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    private final String NOTIFICATION_INTENT_OPEN = "org.dkf.jed2k.android.INTENT_OPEN";

    private final String NOTIFICATION_INTENT_CLOSE = "org.dkf.jed2k.android.INTENT_CLOSE";

    private AtomicBoolean listening = new AtomicBoolean(false);

    private ScheduledFuture scheduledFuture;

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
        Log.v("ED2KService", "onCreate");
        log.debug("ed2k service create");
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        settings.listenPort = 5000;
        session = new Session(settings);
        session.start();
        alertsLoop();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("ED2KService", "onStartCommand");
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

        log.info("ed2k service started by this intent: {} flags {} startId {}", intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.v("ED2KService", "destroy");
        log.debug("ED2K service onDestroy");

        // stop alerts processing
        scheduledExecutorService.shutdown();

        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();

        if (session != null) {
            Log.v("ED2KService", "stop session");
            // stop session
            session.interrupt();
            try {
                session.join();
                Log.v("ED2KService", "session finished");
            } catch (InterruptedException e) {
                log.error("wait session interrupted error {}", e);
            }
        } else {
            log.debug("session is not exist yet");
        }
    }

    synchronized public void addListener(AlertListener listener) {
        listeners.add(listener);
    }

    synchronized public void removeListener(AlertListener listener) {
        listeners.remove(listener);
    }

    synchronized public void processAlert(Alert a) {
        if (a instanceof ListenAlert) {
            Log.v("ED2KService", "listen");
            for(final AlertListener ls: listeners) ls.onListen((ListenAlert)a);
        }
        if (a instanceof SearchResultAlert) {
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
                Log.v("ED2KService", "process alerts");
                Alert a = session.popAlert();
                while(a != null) {
                    processAlert(a);
                    a = session.popAlert();
                }
            }
        },  100, 500, TimeUnit.MILLISECONDS);
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
            Log.e("ED2KService", "Error on search request " + e.toString());
        }
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
}
