package org.dkf.jmule;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.*;
import android.widget.RemoteViews;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.IOUtils;
import org.dkf.jed2k.*;
import org.dkf.jed2k.alert.*;
import org.dkf.jed2k.disk.DesktopFileHandler;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.kad.DhtTracker;
import org.dkf.jed2k.kad.Initiator;
import org.dkf.jed2k.kad.NodeEntry;
import org.dkf.jed2k.protocol.*;
import org.dkf.jed2k.protocol.kad.KadId;
import org.dkf.jed2k.protocol.kad.KadNodesDat;
import org.dkf.jed2k.protocol.server.search.SearchRequest;
import org.slf4j.Logger;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;
import androidx.documentfile.provider.DocumentFile;

public class ED2KService extends JobIntentService {
    public final static int ED2K_STATUS_NOTIFICATION = 0x7ada5021;

    public static final String ACTION_SHOW_TRANSFERS = "org.dkf.jmule.android.ACTION_SHOW_TRANSFERS";
    public static final String ACTION_REQUEST_SHUTDOWN = "org.dkf.jmule.android.ACTION_REQUEST_SHUTDOWN";
    public static final String EXTRA_DOWNLOAD_COMPLETE_NOTIFICATION = "org.dkf.jmule.EXTRA_DOWNLOAD_COMPLETE_NOTIFICATION";
    private static final String DHT_NODES_FILENAME = "dht_nodes.dat";
    private static final String GITHUB_CFG = "https://raw.githubusercontent.com/a-pavlov/jed2k/config/config.json";

    private final static long[] VENEZUELAN_VIBE = buildVenezuelanVibe();
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ED2KService.class);

    public class ED2KServiceBinder extends Binder {
        public ED2KService getService() {
            return ED2KService.this;
        }
    }

    private IBinder binder = new ED2KServiceBinder();

    private boolean vibrateOnDownloadCompleted = false;
    private boolean forwardPorts = false;
    private boolean useDht = false;
    private boolean safeMode = false;
    private KadId kadId = null;

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

    private DhtTracker dhtTracker;

    /**
     * cached hashes of transfers to provide information about hashes we have
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
    volatile ScheduledExecutorService scheduledExecutorService;

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

    final AtomicBoolean permanentNotification = new AtomicBoolean(false);
    private RemoteViews notificationViews;
    private Notification notificationObject;

    /**
     * Localizable Number Format constant for the current default locale.
     */
    private static NumberFormat NUMBER_FORMAT0; // localized "#,##0"

    public static final String GENERAL_UNIT_KBPSEC = "KB/s";

    ResumeDataDbHelper dbHelper;

    static {
        NUMBER_FORMAT0 = NumberFormat.getNumberInstance(Locale.getDefault());
        NUMBER_FORMAT0.setMaximumFractionDigits(0);
        NUMBER_FORMAT0.setMinimumFractionDigits(0);
        NUMBER_FORMAT0.setGroupingUsed(true);
    }

    public static String rate2speed(double rate) {
        return NUMBER_FORMAT0.format(rate) + " " + GENERAL_UNIT_KBPSEC;
    }

    /**
     * Notification manager
     */
    //private NotificationManager mNotificationManager;

    int lastStartId = -1;

    private Set<String> explicitWords = new HashSet<>();
    private Set<Hash> blockedHashes = new HashSet<>();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /**
     * Whenever there's a call to ContextCompat.startForegroundService(context, intent)
     * the service that's supposed to be started in the foreground is expected to perform
     * a service.startForeground() call along with a notification within the next 5 seconds,
     * otherwise you get an IllegalState exception crash for not following the 'contract'
     * @param service
     */
    public static void foregroundServiceStartForAndroidO(Service service) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(
                    Constants.ED2K_NOTIFICATION_CHANNEL_ID,
                    "ED2K",
                    NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE)).
                    createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(
                    service,
                    Constants.ED2K_NOTIFICATION_CHANNEL_ID).
                    setContentTitle("").
                    setContentText("").
                    build();
            service.startForeground(1338, notification);
        }
    }

    @Override
    public void onCreate() {
        log.info("[ED2K service] creating");
        super.onCreate();
        foregroundServiceStartForAndroidO(this);
        // load forbidden words
        try (InputStream ins = getResources().openRawResource(
                getResources().getIdentifier("explicit_words",
                        "raw", getPackageName()))) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
            while (reader.ready()) {
                String line = reader.readLine();
                explicitWords.add(line);
            }

            log.info("[ED2K service] explicit words {}", explicitWords.size());

            // loading blocked hashes
            Container<UInt32, Hash> bh = Container.makeInt(Hash.class);
            if (ConfigurationManager.instance().getSerializable(Constants.PREF_KEY_BLOCK_HASH_LIST, bh) != null) {
                for (final Hash hash : bh.getList()) {
                    blockedHashes.add(hash);
                }
            }

            log.info("[ED2K service] blocked words {}", bh.size());

        } catch (Exception e) {
            log.error("unable to open explicit words or blocked words load failed {}", e.getMessage());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log.info("[ED2K service] start command");
        try {
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();
        } catch(Exception e) {
            log.warn("cancel all notifications error {}", e);
        }

        foregroundServiceStartForAndroidO(this);

        setupNotification();

        if (intent == null) {
            return 0;
        }

        lastStartId = startId;

        log.info("[ED2K service] started by this intent: {} flags {} startId {}", intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        log.info("[ED2K service] destructing...");

        try {
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();
        } catch(Exception e) {
            log.error("[ED2K service] cancel all error");
        }

        if (!blockedHashes.isEmpty()) {
            log.info("[ED2K service] persist blocked hashes");
            Container<UInt32, Hash> bh = Container.makeInt(Hash.class);
            for(final Hash hash: blockedHashes) {
                bh.add(hash);
            }

            ConfigurationManager.instance().setSerializable(Constants.PREF_KEY_BLOCK_HASH_LIST, bh);

            log.info("[ED2K service] store {} hashes", bh.size());
        }


        if (session != null) {
            session.abort();
            try {
                session.join();
                log.info("[ED2K service] session aborted");
            } catch (InterruptedException e) {
                log.error("[ED2K service] wait session interrupted error {}", e);
            }
        }

        // stop alerts processing
        // need additional code to guarantee all alerts were processed
        if (scheduledExecutorService != null) scheduledExecutorService.shutdown();
        log.info("[ED2K service] destroyed");
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        onStartCommand(intent, 0, 1);
    }

    void startSession() {
        if (session != null) return;
        startingInProgress = true;
        session = new Session(settings);
        session.start();
        initializeDatabase();
        startBackgroundOperations();
        startingInProgress = false;

        try {
            if (forwardPorts) session.startUPnP();
            else session.stopUPnP();
        } catch(JED2KException e) {
            log.error("start upnp error {}", e);
        }

        log.info("session started!");
    }

    void stopSession() {
        if (session != null) {
            stoppingInProgress = true;
            session.saveResumeData();
            session.abort();

            try {
                session.join();
            } catch (InterruptedException e) {

            } finally {

                try {
                    if (scheduledExecutorService != null) {
                        scheduledExecutorService.shutdown();
                        scheduledExecutorService.awaitTermination(4, TimeUnit.SECONDS);

                        // catch all remain events and process save resume data
                        if (session != null) {
                            Alert a = session.popAlert();
                            while (a != null) {
                                if (a instanceof TransferResumeDataAlert) {
                                    saveResumeData((TransferResumeDataAlert) a);
                                }
                                a = session.popAlert();
                            }
                        }
                    }
                } catch(InterruptedException e) {
                    log.error("[ED2K service] alert loop await interrupted {}", e.toString());
                }

                session = null;
                scheduledExecutorService = null;
            }

            stoppingInProgress = false;
        }
    }

    public ResumeDataDbHelper initializeDatabase() {
        if (dbHelper == null) {
            dbHelper = new ResumeDataDbHelper(getApplicationContext());
        }

        return dbHelper;
    }

    /**
     * synchronized methods to avoid simultaneous unsynchronized access to dhtTracker variable from different threads
     */
    synchronized void startDht() {
        if (useDht) {
            dhtTracker = new DhtTracker(settings.listenPort, kadId, null);
            dhtTracker.start();
            Container<UInt32, NodeEntry> entries = loadDhtEntries();
            if (entries != null && entries.getList() != null) {
                dhtTracker.addEntries(entries.getList());
            }

            session.setDhtTracker(dhtTracker);
            // unsynchronized check here - actually executor service must be created already
            if (scheduledExecutorService != null) {
                scheduledExecutorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        refreshDhtStoragePoint();
                    }
                });
            }
        }
    }

    synchronized void stopDht() {
        if (dhtTracker != null) {
            saveDhtEntries(dhtTracker.getTrackerState());
            dhtTracker.abort();
            session.setDhtTracker(null);
            try {
                dhtTracker.join();
            } catch (InterruptedException e) {
                log.error("[ED2K service] stop DHT tracker interrupted {}", e);
            } finally {
                dhtTracker = null;
            }
        }
    }

    public synchronized boolean isDhtEnabled() {
        return dhtTracker != null;
    }

    /**
     * synchronized save call to avoid racing on access to dhtTracker variable from
     * main thread(start/stop dht) and background service(save)
     */
    synchronized void saveDhtState() {
        if (dhtTracker != null) {
            saveDhtEntries(dhtTracker.getTrackerState());
        }
    }

    public synchronized boolean addNodes(final KadNodesDat nodes) {
        assert nodes != null;
        if (dhtTracker != null) {
            dhtTracker.addKadEntries(nodes.getContacts());
            return true;
        } else {
            log.warn("tracker or nodes is null");
        }

        return false;
    }

    public synchronized void setDhtStoragePoint(final GithubConfigurator ghCfg) {
        if (dhtTracker != null) {
            // not configured storage point
            if (ghCfg.getKadStorageDescription() == null) {
                dhtTracker.setStoragePoint(null);
            } else {
                Random rnd = new Random();
                try {
                    InetSocketAddress address = new InetSocketAddress(ghCfg.getKadStorageDescription().getIp()
                            , ghCfg.getKadStorageDescription().getPorts().get(rnd.nextInt(ghCfg.getKadStorageDescription().getPorts().size())));
                    dhtTracker.setStoragePoint(address);
                    Endpoint sp = Endpoint.fromInet(address);
                    dhtTracker.addRouterNodes(new Endpoint(sp.getIP(), sp.getPort() + 1));
                    log.info("storage point configured to {} router node configured to {}"
                            , address
                            , new Endpoint(sp.getIP(), sp.getPort() + 1));
                } catch(Exception e) {
                    log.warn("Unable to configure storage point address {}", e);
                }
            }
        }
    }

    GithubConfigurator getConfiguration(final String url) throws Exception {
        byte[] data = IOUtils.toByteArray(new URI(url));
        Gson gson = new GsonBuilder().create();
        String s = null;
        s = new String(data);
        return gson.fromJson(s, GithubConfigurator.class);
    }

    private void refreshDhtStoragePoint() {
        try {
            GithubConfigurator ghCfg = getConfiguration(GITHUB_CFG);
            ghCfg.validate();
            setDhtStoragePoint(ghCfg);
        } catch(Exception e) {
            log.error("unable to refresh kad storage point {}", e);
        }
    }

    /**
     *
     * @return loaded entries from config file or null
     */
    private Container<UInt32, NodeEntry> loadDhtEntries() {
        FileInputStream stream = null;
        FileChannel channel = null;
        try {
            stream = openFileInput(DHT_NODES_FILENAME);
            channel = stream.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate((int)channel.size());
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            channel.read(buffer);
            buffer.flip();
            Container<UInt32, NodeEntry> entries = Container.makeInt(NodeEntry.class);
            entries.get(buffer);
            return entries;
        } catch(FileNotFoundException e) {
            log.info("[ED2K service] dht nodes not found {}", e);
        } catch(IOException e) {
            log.error("[ed2k service] i/o exception on load dht nodes {}", e);
        } catch(JED2KException e) {
            log.error("[ed2k service] internal error on load dht nodes {}", e);
        } catch(Exception e) {
            log.error("[ed2k service] unexpected error on load dht nodes {}", e);
        }
        finally {
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException e) {
                    //just ignore it
                }
            }

            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // just ignore it
                }
            }
        }

        return null;
    }

    /**
     * saves DHT nodes to config file
     * @param entries from DHT tracker
     */
    private void saveDhtEntries(final Container<UInt32, NodeEntry> entries) {
        if (entries != null) {
            FileOutputStream stream = null;
            FileChannel channel = null;
            try {
                stream = openFileOutput(DHT_NODES_FILENAME, 0);
                channel = stream.getChannel();
                ByteBuffer buffer = ByteBuffer.allocate(entries.bytesCount());
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                entries.put(buffer);
                buffer.flip();
                channel.write(buffer);
                log.info("[edk2 service] save dht entries {}", entries.size());
            } catch(FileNotFoundException e) {
                log.error("[ed2k service] unable to open output stream for dht nodes {}", e);
            } catch(JED2KException e) {
                log.error("[ed2k service] internal error on save dht nodes {}", e);
            } catch(IOException e) {
                log.error("[ed2k service] i/o error {}", e);
            } catch(Exception e) {
                log.error("[ed2k service] unexpected error {}", e);
            }
            finally {
                if (channel != null) {
                    try {
                        channel.close();
                    } catch(IOException e) {
                        //just ignore it
                    }
                }

                if (stream != null) {
                    try {
                        stream.close();
                    } catch(IOException e) {
                        // just ignore it
                    }
                }
            }
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
        dbHelper.removeResumeData(hash);
    }

    private void createTransferNotification(final String title, final String extra, final Hash hash) {
        if (session != null) {
            TransferHandle handle = session.findTransfer(hash);
            if (handle.isValid()) {
                File f = handle.getFile();
                buildNotification(title, f != null ? f.getName() : "", extra);
            }
        }
    }

    private void saveResumeData(final TransferResumeDataAlert alert) {
        /*
        FileOutputStream stream = null;
        try {
            stream = openFileOutput("rd_" + alert.hash.toString(), MODE_PRIVATE);
            ByteBuffer bb = ByteBuffer.allocate(alert.trd.bytesCount());
            alert.trd.put(bb);
            bb.flip();
            stream.write(bb.array(), 0, bb.limit());
            log.info("[ED2K service] saved resume data {} size {}", alert.hash.toString(), alert.trd.bytesCount());
        } catch(FileNotFoundException e) {
            log.error("[ED2K service] save resume data {} failed {}"
                    , alert.hash, e);
        } catch(IOException e) {
            log.error("[ED2K service] save resume data write {} failed {}"
                    , alert.hash, e);
        }
        catch(JED2KException e) {
            log.error("[ED2K service] save resume data serialization {} failed {}"
                    , alert.hash, e);
        }
        catch(Exception e) {
            log.error("[ED2K service] save resume data exception {}", e);
        }
        finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch(IOException e) {
                    // just ignore
                    log.error("[ED2K service] save resume data close stream failed {}"
                            , e.toString());
                }
            }
        }
        */
        try {
            log.info("save resume data {}", alert.hash);
            dbHelper.saveResumeData(alert.trd);
        } catch (JED2KException e) {
            log.error("[ED2K service] save resume data {} failed {}"
                    , alert.hash, e);
        } catch (Throwable e) {
            log.error("save resume data error {}", e.getMessage());
        }
    }

    public void processAlert(final Alert a) {
        try {
            if (a instanceof ListenAlert) {
                for (final AlertListener ls : listeners) ls.onListen((ListenAlert) a);
            } else if (a instanceof SearchResultAlert) {
                // inplace filtering bad words in case when search is limited or we have blocked hashes dictionary
                if (safeMode || !blockedHashes.isEmpty()) {
                    SearchResultAlert sa = (SearchResultAlert) a;
                    Iterator<SearchEntry> itr = sa.getResults().iterator();
                    while(itr.hasNext()) {
                        SearchEntry se = itr.next();
                        if ((safeMode && isFiltered(se.getFileName())) || isBlocked(se.getHash())) {
                            itr.remove();
                        }
                    }
                }
                for (final AlertListener ls : listeners) ls.onSearchResult((SearchResultAlert) a);
            } else if (a instanceof ServerMessageAlert) {
                for (final AlertListener ls : listeners) ls.onServerMessage((ServerMessageAlert) a);
            } else if (a instanceof ServerStatusAlert) {
                for (final AlertListener ls : listeners) ls.onServerStatus((ServerStatusAlert) a);
            } else if (a instanceof ServerConectionClosed) {
                for (final AlertListener ls : listeners) ls.onServerConnectionClosed((ServerConectionClosed) a);
            } else if (a instanceof ServerIdAlert) {
                for (final AlertListener ls : listeners) ls.onServerIdAlert((ServerIdAlert) a);
            } else if (a instanceof ServerConnectionAlert) {
                for (final AlertListener ls : listeners) ls.onServerConnectionAlert((ServerConnectionAlert) a);
            } else if (a instanceof TransferResumedAlert) {
                for (final AlertListener ls : listeners) ls.onTransferResumed((TransferResumedAlert) a);
            } else if (a instanceof TransferPausedAlert) {
                for (final AlertListener ls : listeners) ls.onTransferPaused((TransferPausedAlert) a);
            } else if (a instanceof TransferAddedAlert) {
                localHashes.put(((TransferAddedAlert) a).hash, 0);
                log.info("[ED2K service] new transfer added {} save resume data now", ((TransferAddedAlert) a).hash);
                session.saveResumeData();
                for (final AlertListener ls : listeners) ls.onTransferAdded((TransferAddedAlert) a);
            } else if (a instanceof TransferRemovedAlert) {
                log.info("[ED2K service] transfer removed {}", ((TransferRemovedAlert) a).hash);
                localHashes.remove(((TransferRemovedAlert) a).hash);
                removeResumeDataFile(((TransferRemovedAlert) a).hash);
                for (final AlertListener ls : listeners) ls.onTransferRemoved((TransferRemovedAlert) a);
            } else if (a instanceof TransferResumeDataAlert) {
                saveResumeData((TransferResumeDataAlert) a);
            } else if (a instanceof TransferFinishedAlert) {
                log.info("[ED2K service] transfer finished {} save resume data", ((TransferFinishedAlert) a).hash);
                session.saveResumeData();
                notificationHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        createTransferNotification(getResources().getString(R.string.transfer_finished), EXTRA_DOWNLOAD_COMPLETE_NOTIFICATION, ((TransferFinishedAlert) a).hash);
                    }
                });
            } else if (a instanceof TransferDiskIOErrorAlert) {
                TransferDiskIOErrorAlert errorAlert = (TransferDiskIOErrorAlert) a;
                log.error("[ED2K service] disk i/o error: {}", errorAlert.ec);
                long lastIOErrorTime = 0;
                if (transfersIOErrorsOrder.containsKey(errorAlert.hash)) {
                    lastIOErrorTime = transfersIOErrorsOrder.get(errorAlert.hash);
                }

                transfersIOErrorsOrder.put(errorAlert.hash, errorAlert.getCreationTime());

                // dispatch alert if no i/o errors on this transfer in last 10 seconds
                if (errorAlert.getCreationTime() - lastIOErrorTime > 10 * 1000) {
                    for (final AlertListener ls : listeners) ls.onTransferIOError(errorAlert);
                    notificationHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            createTransferNotification(getResources().getString(R.string.transfer_io_error), "", ((TransferDiskIOErrorAlert) a).hash);
                        }
                    });
                }
            } else if (a instanceof PortMapAlert) {
                for (final AlertListener ls : listeners) ls.onPortMapAlert((PortMapAlert) a);
                log.info("[ED2K service] port mapped {} {}", ((PortMapAlert)a).port, ((PortMapAlert)a).ec.getDescription());
            }
            else {
                log.debug("[ED2K service] received unhandled alert {}", a);
            }
        }
        catch(Exception e) {
            log.error("[ED2K service] processing alert {} error {}", a, e);
        }
    }

    private void restoreTransfersFromDB() {
        try (ResumeDataDbHelper.ATPIterator itrAtp = dbHelper.iterator()) {
            while (itrAtp.hasNext()) {
                AddTransferParams atp = itrAtp.next();
                TransferHandle handle = null;

                if (atp != null) {
                    File file = new File(atp.getFilepath().asString());
                    if (Platforms.get().saf()) {
                        log.info("[ED2k service] restore file {}", file.getName());
                        LollipopFileSystem fs = (LollipopFileSystem) Platforms.fileSystem();
                        android.util.Pair<ParcelFileDescriptor, DocumentFile> targetFile = fs.openFD(file, "rw");
                        if (targetFile != null && targetFile.second != null && targetFile.first != null && targetFile.second.exists()) {
                            atp.setExternalFileHandler(new AndroidFileHandler(file, targetFile.second, targetFile.first));
                            handle = session.addTransfer(atp);
                        } else {
                            log.error("[ED2K service] restore transfer {} failed document/parcel is null", file);
                        }
                    } else {
                        if (file.exists()) {
                            atp.setExternalFileHandler(new DesktopFileHandler(file));
                            handle = session.addTransfer(atp);
                        } else {
                            log.warn("[ED2K service] unable to restore transfer {}: file not exists", file);
                        }
                    }
                }

                if (handle != null) {
                    log.info("transfer {} is {}"
                            , handle.isValid() ? handle.getHash().toString() : ""
                            , handle.isValid() ? "valid" : "invalid");
                }
            }
        } catch (Exception e) {

        }
    }

    // obsolete method - after restore transfers from file file will be removed
    // save resume data saves data to database
    private boolean restoreTransferFromFile(File f) {
        long fileSize = f.length();

        if (fileSize > org.dkf.jed2k.Constants.BLOCK_SIZE_INT) {
            log.warn("[ED2K service] resume data file {} has too large size {}, skip it"
                    , f.getName(), fileSize);
            return false;
        }

        TransferHandle handle = null;
        ByteBuffer buffer = ByteBuffer.allocate((int)fileSize);
        try(FileInputStream istream = openFileInput(f.getName())) {
            log.info("[ED2K service] load resume data {} size {}"
                    , f.getName()
                    , fileSize);

            istream.read(buffer.array(), 0, buffer.capacity());
            // do not flip buffer!
            AddTransferParams atp = new AddTransferParams();
            atp.get(buffer);
            File file = new File(atp.getFilepath().asString());

            if (Platforms.get().saf()) {
                LollipopFileSystem fs = (LollipopFileSystem)Platforms.fileSystem();
                if (fs.exists(file)) {
                    android.util.Pair<ParcelFileDescriptor, DocumentFile> resume = fs.openFD(file, "rw");

                    if (resume != null && resume.second != null && resume.first != null && resume.second.exists()) {
                        atp.setExternalFileHandler(new AndroidFileHandler(file, resume.second, resume.first));
                        handle = session.addTransfer(atp);
                    } else {
                        log.error("[ED2K service] restore transfer {} failed document/parcel is null", file);
                    }
                } else {
                    log.warn("[ED2K service] unable to restore transfer {}: file not exists", file);
                }
            } else {
                if (file.exists()) {
                    atp.setExternalFileHandler(new DesktopFileHandler(file));
                    handle = session.addTransfer(atp);
                } else {
                    log.warn("[ED2K service] unable to restore transfer {}: file not exists", file);
                }
            }
        }
        catch(FileNotFoundException e) {
            log.error("[ED2K service] load resume data file not found {} error {}", f.getName(), e);
        }
        catch(IOException e) {
            log.error("[ED2K service] load resume data {} i/o error {}", f.getName(), e);
        }
        catch(JED2KException e) {
            log.error("[ED2K service] load resume data {} add transfer error {}", f.getName(), e);
        }

        // log transfer handle if it has been added
        if (handle != null) {
            log.info("transfer {} is {}"
                    , handle.isValid() ? handle.getHash().toString() : ""
                    , handle.isValid() ? "valid" : "invalid");
        }

        return handle != null;
    }

    private void startBackgroundOperations() {
        assert(session != null);
        assert(scheduledExecutorService == null);
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
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
                saveDhtState();
            }
        }, 60, 200, TimeUnit.SECONDS);

        // every 5 seconds execute permanent notification
        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                updatePermanentStatusNotification();
            }
        }, 1, 6, TimeUnit.SECONDS);

        scheduledExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if (session == null) {
                    log.warn("[ED2K service] session is null, stop resume data loading");
                }

                File fd = getFilesDir();
                File[] files = fd.listFiles(new java.io.FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return (pathname.getName().startsWith("rd_"));
                    }
                });

                if (files != null) {
                    for(final File resumeDataFile: files) {
                        if (!restoreTransferFromFile(resumeDataFile)) {
                            Platforms.fileSystem().delete(resumeDataFile);
                        }
                    }
                } else {
                    log.info("[ED2K service] have no resume data files");
                }

                // restore transfers from database
                restoreTransfersFromDB();
            }
        });

        // every 1 minute execute bootstrapping check
        scheduledExecutorService.scheduleWithFixedDelay(new Initiator(session), 1, 1, TimeUnit.MINUTES);

        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                refreshDhtStoragePoint();
            }
        }, 10, 20, TimeUnit.MINUTES);
    }

    private void updatePermanentStatusNotification() {
        try {
            if (!permanentNotification.get()) return;

            if (notificationViews == null || notificationObject == null) {
                log.warn("[ED2K service] Notification views or object are null, review your logic");
                return;
            }

            //  format strings
            String sDown = rate2speed(getDownloadUploadRate().left / 1024);

            // number of uploads (seeding) and downloads
            int downloads = getTransfers().size();

            // Transfers status.
            notificationViews.setTextViewText(R.id.view_permanent_status_text_downloads, downloads + " @ " + sDown);

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (manager != null) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel(Constants.ED2K_NOTIFICATION_CHANNEL_ID, "ED2K", NotificationManager.IMPORTANCE_MIN);
                    channel.setSound(null, null);
                    manager.createNotificationChannel(channel);
                }

                manager.notify(ED2K_STATUS_NOTIFICATION, notificationObject);
            }
        } catch (Throwable e) {
            log.error("Permanent notification finished {}", e);
        }
    }

    private void setupNotification() {
        RemoteViews remoteViews = new RemoteViews(this.getPackageName(),
                R.layout.view_permanent_status_notification);

        PendingIntent showFrostWireIntent = createShowFrostwireIntent();
        PendingIntent shutdownIntent = createShutdownIntent();

        remoteViews.setOnClickPendingIntent(R.id.view_permanent_status_shutdown, shutdownIntent);
        remoteViews.setOnClickPendingIntent(R.id.view_permanent_status_text_title, showFrostWireIntent);

        Notification notification = new NotificationCompat.Builder(getApplicationContext(), Constants.ED2K_NOTIFICATION_CHANNEL_ID).
                setSmallIcon(R.drawable.notification_mule).
                setContentIntent(showFrostWireIntent).
                setContent(remoteViews).
                build();

        notification.flags |= Notification.FLAG_NO_CLEAR;

        notificationViews = remoteViews;
        notificationObject = notification;
    }

    private PendingIntent createShowFrostwireIntent() {
        return PendingIntent.getActivity(getApplicationContext(),
                0,
                new Intent(ACTION_SHOW_TRANSFERS)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK),
                0);
    }

    private PendingIntent createShutdownIntent() {
        return PendingIntent.getActivity(getApplicationContext(),
                1,
                new Intent(ACTION_REQUEST_SHUTDOWN).
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK),
                0);
    }

    private void buildNotification(final String title, final String summary, final String extra) {
        try {
            Intent intentShowTransfers = new Intent(ACTION_SHOW_TRANSFERS);
            if (!extra.isEmpty()) intentShowTransfers.putExtra(extra, true);

            /**
             * Pending intents
             */
            PendingIntent openPending = PendingIntent.getActivity(getApplicationContext(), 0, intentShowTransfers, 0);

            /**
             * Remote view for normal view
             */
            Bitmap art = BitmapFactory.decodeResource(getResources(), R.drawable.notification_mule);

            RemoteViews mNotificationTemplate = new RemoteViews(this.getPackageName(), R.layout.notification);
            Notification.Builder notificationBuilder = new Notification.Builder(this);

            mNotificationTemplate.setTextViewText(R.id.notification_line_one, title);
            mNotificationTemplate.setTextViewText(R.id.notification_line_two, summary);
            //mNotificationTemplate.setImageViewResource(R.id.notification_play, R.drawable.btn_playback_pause /* : R.drawable.btn_playback_play*/);
            //mNotificationTemplate.setImageViewBitmap(R.id.notification_image, art);

            /**
             * OnClickPending intent for collapsed notification
             */
            //mNotificationTemplate.setOnClickPendingIntent(R.id.notification_collapse, openPending);

            Context context = getApplicationContext();
            PendingIntent pi = PendingIntent.getActivity(context, 0, intentShowTransfers, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            /*Notification notification = new NotificationCompat.Builder(context, Constants.ED2K_NOTIFICATION_CHANNEL_ID)
                    .setWhen(System.currentTimeMillis())
                    .setContentText(summary)
                    .setContentTitle(title)
                    .setSmallIcon(R.drawable.notification_mule)
                    .setContentIntent(pi)
                    .build();*/

            Notification notification = new NotificationCompat.Builder(context, Constants.ED2K_NOTIFICATION_CHANNEL_ID)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.notification_mule)
                    .setContentIntent(openPending)
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    .setContent(mNotificationTemplate)
                    .setUsesChronometer(true)
                    .build();

            notification.vibrate = ConfigurationManager.instance().vibrateOnFinishedDownload() ? VENEZUELAN_VIBE : null;
            //notification.number = TransferManager.instance().getDownloadsToReview();
            //notification.flags |= Notification.FLAG_AUTO_CANCEL;

            if (manager != null) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel(Constants.ED2K_NOTIFICATION_CHANNEL_ID, "ED2K", NotificationManager.IMPORTANCE_MIN);
                    channel.setSound(null, null);
                    manager.createNotificationChannel(channel);
                }
                manager.notify(Constants.NOTIFICATION_DOWNLOAD_TRANSFER_FINISHED, notification);
            }

            /**
             * Create notification instance
             */
            /*Notification notification = notificationBuilder
                    .setSmallIcon(R.drawable.notification_mule)
                    .setContentIntent(openPending)
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    .setContent(mNotificationTemplate)
                    .setUsesChronometer(true)
                    .build();

            notification.vibrate = vibrateOnDownloadCompleted?VENEZUELAN_VIBE:null;
            */
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
            //NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            //if (manager != null) manager.notify(NOTIFICATION_ID, notification);
        } catch (Throwable e) {
            log.error("Error creating notification for download finished {}", e);
        }
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

    public void startSearchDhtKeyword(final String keyword
            , final long minSize
            , final long maxSize
            , final int sources
            , final int completeSources) {
        if (session == null) return;
        session.searchDhtKeyword(keyword, minSize, maxSize, sources, completeSources);
    }

    /**
     * search more results, run only if search result has flag more results
     */
    public void searchMore() {
        if (session != null) session.searchMore();
    }

    public void startServices() {
        startSession();
        startDht();
    }

    public void stopServices() {
        stopDht();
        stopSession();
    }

    public void shutdown(){
        stopServices();
        stopForeground(true);
        stopSelf(lastStartId);
        boolean b = stopSelfResult(lastStartId);
        log.info("stop self {} last id: {}", b?"true":"false", lastStartId);
    }

    public TransferHandle addTransfer(final Hash hash, final long fileSize, final File file)
            throws JED2KException {
        if(session != null) {
            log.info("[ED2K service] start transfer {} file {} size {}"
                    , hash.toString()
                    , file
                    , fileSize);

            if (Platforms.get().saf()) {
                LollipopFileSystem fs = (LollipopFileSystem)Platforms.fileSystem();
                android.util.Pair<ParcelFileDescriptor, DocumentFile> fd = fs.openFD(file, "rw");

                if (fd != null && fd.second != null && fd.first != null) {
                    AndroidFileHandler handler = new AndroidFileHandler(file, fd.second, fd.first);
                    return session.addTransfer(hash, fileSize, handler);
                } else {
                    log.error("unable to create target file {}", file);
                    throw new JED2KException(ErrorCode.IO_EXCEPTION);
                }
            } else {
                return session.addTransfer(hash, fileSize, file);
            }
        }

        log.error("[ED2K service] session is null, but requested transfer adding");
        throw new JED2KException(ErrorCode.INTERNAL_ERROR);
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
            log.info("configure session: {}", settings.toString());
            session.configureSession(settings);
        }
    }

    public void setNickname(final String name) {
        settings.clientName = name;
    }

    public void setVibrateOnDownloadCompleted(boolean vibrate) {
        this.vibrateOnDownloadCompleted = vibrate;
    }

    public void setForwardPort(boolean forward) {
        try {
            forwardPorts = forward;
            if (session != null) {
                if (forward) {
                    session.startUPnP();
                } else {
                    session.stopUPnP();
                }
            }
        } catch(JED2KException e) {
            log.error("upnp command raised exception {}", e);
        }
    }

    public void useDht(boolean useDht) {
        this.useDht = useDht;
        if (session != null) {
            if (useDht) {
                startDht();
            } else {
                stopDht();
            }
        }
    }

    public void setSafeMode(boolean value) {
        safeMode = value;
    }

    public boolean isSafeMode() {
        return safeMode;
    }

    public boolean isFiltered(String value) {
        String values[] = value.toLowerCase().split("-|\\_|\\.|,|\\s|\\}|\\{|\\(|\\)");
        for(final String s: values) {
            if (explicitWords.contains(s)) {
                return true;
            }
        }
        return false;
    }

    public void blockHash(Hash hash) {
        blockedHashes.add(hash);
    }

    public boolean isBlocked(Hash hash) {
        return blockedHashes.contains(hash);
    }

    public void setListenPort(int port) {
        settings.listenPort = port;
    }

    public void setServerReconnect(boolean value) {
        settings.reconnectoToServer = value;
    }

    public void setServerPing(boolean value) { settings.serverPingTimeout = value?60:0; }

    public void setMaxPeerListSize(int maxSize) {
        settings.maxPeerListSize = maxSize;
    }

    public void setUserAgent(Hash hash) {
        settings.userAgent = hash;
    }

    public void setKadId(final KadId id) {
        kadId = id;
    }

    public Pair<Long, Long> getDownloadUploadRate() {
        if (session != null) {
            return session.getDownloadUploadRate();
        }

        return Pair.make(0l, 0l);
    }

    public int getTotalDhtNodes() {
        if (dhtTracker != null) {
            Pair<Integer, Integer> sz = dhtTracker.getRoutingTableSize();
            return sz.getLeft() + sz.getRight();
        }

        return -1;
    }

    private static long[] buildVenezuelanVibe() {

        long shortVibration = 80;
        long mediumVibration = 100;
        long shortPause = 100;
        long mediumPause = 150;
        long longPause = 180;

        return new long[]{0, shortVibration, longPause, shortVibration, shortPause, shortVibration, shortPause, shortVibration, mediumPause, mediumVibration};
    }

    /**
     * firstly setup notification and prepare all controls
     * next set flag to avoid unsynchronized access to controls
     * @param value
     */
    public void setPermanentNotification(boolean value) {
        permanentNotification.set(value);
    }

    public ResumeDataDbHelper getDBHelper() {
        return dbHelper;
    }
/*
    private static void cancelAllNotificationsTask(EngineService engineService) {
        try {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancelAll();
            }
        } catch (SecurityException ignore) {
            // new exception in Android 7
        }
    }

    private static void startPermanentNotificationUpdatesTask(EngineService engineService) {
        try {
            if (engineService.notificationUpdateDemon == null) {
                engineService.notificationUpdateDemon = new NotificationUpdateDemon(engineService.getApplicationContext());
            }
            engineService.notificationUpdateDemon.start();
        } catch (Throwable t) {
            LOG.warn(t.getMessage(), t);
        }
    }
    */
}
