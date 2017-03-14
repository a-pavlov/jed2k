package org.dkf.jed2k;

import org.apache.commons.io.IOUtils;
import org.dkf.jed2k.alert.*;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.kad.DhtTracker;
import org.dkf.jed2k.kad.Initiator;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.Hash;
import org.dkf.jed2k.protocol.SearchEntry;
import org.dkf.jed2k.protocol.kad.KadId;
import org.dkf.jed2k.protocol.kad.KadNodesDat;
import org.dkf.jed2k.protocol.server.search.SearchRequest;
import org.dkf.jed2k.util.FUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class Conn {
    private static Logger log = LoggerFactory.getLogger(Conn.class);
    private static List<SearchEntry> globalSearchRes = null;
    private static boolean hasMoreResults = false;
    private static final boolean trial = "true".equals(System.getProperty("session.trial"));
    private static final boolean compression = "true".equals(System.getProperty("session.compression"));
    private static Set<TransferHandle> handles = new HashSet<>();
    private static Path incomingDirectory;
    private static Path resumeDataDirectory;
    private static int GLOBAL_PORT = 9999;
    private static final String DHT_STATUS_FILENAME = "conn_dht_status.dat";

    private static String report(final Session s) {
        StringBuilder sb = new StringBuilder();
        List<TransferHandle> handles = s.getTransfers();
        sb.append("Transfers: \n");
        for(final TransferHandle h: handles) {
            sb.append(h.getHash().toString()).append("{").append(h.getSize()).append("}");
            TransferStatus status = h.getStatus();
            sb.append("\n").append(status).append("\n");
            List<PeerInfo> peers = h.getPeersInfo();
            for(final PeerInfo pi: peers) {
                sb.append("    ").append(pi).append("\n");
            }
        }

        return sb.toString();
    }

    private static void printGlobalSearchResult() {
        if (globalSearchRes == null) return;
        int index = 0;
        for(SearchEntry entry: globalSearchRes) {
            log.info("-> {} {}", String.format("%03d ", index++), entry.toString());
        }

        log.info("more results: {}", (hasMoreResults?"yes":"no"));
    }

    static TransferHandle addTransfer(final Session s, final Hash hash, final long size, final File file) {
        try {
            TransferHandle h = s.addTransfer(hash, size, file);
            if (h.isValid()) {
                log.info("[CONN] transfer valid {}", h.getHash());
            }

            return h;
        } catch (JED2KException e) {
            log.warn("[CONN] add transfer failed {}", e.toString());
        }

        return null;
    }

    static void saveTransferParameters(final AddTransferParams params) throws JED2KException {
        File transferFile = new File(params.getFilepath().asString());
        File resumeDataFile = new File(resumeDataDirectory.resolve(transferFile.getName()).toString());

        try(FileOutputStream stream = new FileOutputStream(resumeDataFile, false); FileChannel channel = stream.getChannel();) {
            ByteBuffer bb = ByteBuffer.allocate(params.bytesCount());
            bb.order(ByteOrder.LITTLE_ENDIAN);
            params.put(bb);
            bb.flip();
            while(bb.hasRemaining()) channel.write(bb);
        } catch(IOException e) {
            log.error("[CONN] I/O exception on save resume data {}", e);
        } catch(JED2KException e) {
            log.error("[CONN] unable to load search results {}", e);
        }
    }

    public static void main(String[] args) throws IOException, JED2KException {

        if (args.length < 1) {
            log.warn("[CONN] specify incoming directory");
            return;
        }

        incomingDirectory = FileSystems.getDefault().getPath(args[0]);
        log.info("[CONN] incoming directory set to: {}", incomingDirectory);
        File incomingFile = incomingDirectory.toFile();
        boolean dirCreated = incomingFile.exists() || incomingFile.mkdirs();

        if (!dirCreated) {
            throw new JED2KException(ErrorCode.INCOMING_DIR_INACCESSIBLE);
        }

        resumeDataDirectory = incomingDirectory.resolve(".resumedata");
        File resumeFile = resumeDataDirectory.toFile();

        dirCreated = resumeFile.exists() || resumeFile.mkdirs();

        if (!dirCreated) {
            throw new JED2KException(ErrorCode.INCOMING_DIR_INACCESSIBLE);
        }

        assert incomingDirectory != null;
        assert resumeDataDirectory != null;
        DhtTracker tracker = null;

        log.info("[CONN] started");
        final Settings startSettings = new Settings();
        startSettings.maxConnectionsPerSecond = 10;
        startSettings.sessionConnectionsLimit = 100;
        startSettings.compressionVersion = compression?1:0;
        startSettings.serverPingTimeout = 0;
        startSettings.listenPort = GLOBAL_PORT;

        LinkedList<Endpoint> systemPeers = new LinkedList<Endpoint>();
        String sp = System.getProperty("session.peers");
        if (sp != null) {
            String[] strP = sp.split(",");
            for (final String s : strP) {
                String[] strEndpoint = s.split(":");

                if (strEndpoint.length == 2) {
                    Endpoint ep = new Endpoint(new InetSocketAddress(strEndpoint[0], (short) Integer.parseInt(strEndpoint[1])));
                    systemPeers.addLast(ep);
                    log.debug("add system peer: {}", ep);
                } else {
                    log.warn("Incorrect endpoint {}", s);
                }
            }
        }

        String hashSession = System.getProperty("session.hash");
        if (hashSession != null) {
            startSettings.userAgent = Hash.fromString(hashSession);
        }

        final Session s = (trial)?(new SessionTrial(startSettings, systemPeers)):(new Session(startSettings));
        // add sources here
        log.info("Kind of session now: {}", s);
        log.info("Settings: {}", startSettings);
        s.start();

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

        ScheduledFuture scheduledFuture =
            scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                    Alert a = s.popAlert();
                    while(a != null) {
                        if (a instanceof SearchResultAlert) {
                            List<SearchEntry> se = ((SearchResultAlert)a).getResults();
                            globalSearchRes = se;
                            globalSearchRes.sort(new Comparator<SearchEntry>() {
                                @Override
                                public int compare(SearchEntry o1, SearchEntry o2) {
                                    if (o1.getSources() < o2.getSources()) return -1;
                                    if (o1.getSources() > o2.getSources()) return 1;
                                    return 0;
                                }
                            });
                            printGlobalSearchResult();
                        }
                        else if (a instanceof ServerMessageAlert) {
                            log.info("[CONN] server message: " + ((ServerMessageAlert)a).msg);
                        }
                        else if (a instanceof ServerStatusAlert) {
                            ServerStatusAlert ssa = (ServerStatusAlert)a;
                            log.info("[CONN] files count: {} users count: {}", ssa.filesCount, ssa.usersCount);
                        }
                        else if (a instanceof ServerInfoAlert) {
                            log.info("[CONN] server info: {}", ((ServerInfoAlert)a).info);
                        }
                        else {
                            log.info("[CONN] unknown alert received: {}", a.toString());
                        }

                        a = s.popAlert();
                    }
                }
            },
        100, 400,
        TimeUnit.MILLISECONDS);

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String command;

        DhtInitialData idata = new DhtInitialData();
        try {
            FUtils.read(idata, new File(incomingDirectory.resolve(DHT_STATUS_FILENAME).toString()));
        } catch(JED2KException e) {
            log.warn("[CONN] read initial data failed {}", e);
        }

        if (idata.getTarget().isAllZeros()) {
            idata.setTarget(new KadId(Hash.random(false)));
        }

        while ((command = in.readLine()) != null) {
            String[] parts = command.split("\\s+");

            if (parts[0].compareTo("exit") == 0 || parts[0].compareTo("quit") == 0) {
                if (tracker != null) {
                    idata.setEntries(tracker.getTrackerState());
                    tracker.abort();
                    try {
                        tracker.join();
                        log.info("[CONN] DHT tracker finished");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                s.abort();
                try {
                    s.join();
                    log.info("[CONN] session finished");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            }

            if (parts[0].compareTo("listen") == 0 && parts.length == 2) {
            	Settings settings = new Settings();
            	settings.listenPort = Integer.parseInt(parts[1]);
            	s.configureSession(settings);
            }
            if (parts[0].compareTo("connect") == 0 && parts.length >= 2) {
                s.connectoTo("server", parts[1], Integer.parseInt((parts.length > 2)?parts[2]:"4661"));
            }
            else if (parts[0].compareTo("search2") == 0) {
                try {
                    log.info("search request: game AND thrones");
                    s.search(SearchRequest.makeRequest(0, 0, 0, 0, "", "", "", 0, 0, "game AND thrones"));
                } catch(JED2KException e) {
                    log.error("[CONN] {}", e);
                }
            }
            else if (parts[0].compareTo("search") == 0 && parts.length > 1) {
                String searchExpression = parts[1];
                long maxSize = 0;
                int sources = 0;
                if (parts.length > 3) {
                    if (parts[2].compareTo("dataSize") == 0) {
                        maxSize = Integer.parseInt(parts[3])*1024*1024;
                    }
                }
                log.info("search expression: {} max dataSize {}", searchExpression, maxSize);
                try {
                    log.info("search request: " + s);
                    s.search(SearchRequest.makeRequest(0, maxSize, 0, 0, "", "", "", 0, 0, searchExpression));
                } catch(JED2KException e) {
                    log.error("[CONN] {}", e);
                }
            }
            else if (parts[0].compareTo("dsearch") == 0 && parts.length == 2) {
                int index = Integer.parseInt(parts[1]);
                if (index >= globalSearchRes.size() || index < 0) {
                    log.warn("[CONN] specified index {} out of last search result bounds {}"
                            , index
                            , globalSearchRes.size());
                } else {
                    SearchEntry sfe = globalSearchRes.get(index);
                    long filesize = sfe.getFileSize();

                    if (filesize != 0) {
                        log.debug("[CONN] start search {}/{}", sfe.getHash(), filesize);
                        s.dhtDebugSearch(sfe.getHash(), filesize);
                    } else {
                        log.warn("[CONN] unable to start DHT debug search due to zero file size");
                    }
                }
            }
            else if (parts[0].compareTo("dhtsearch") == 0 && parts.length == 3) {
                Hash fileHash = Hash.fromString(parts[1]);
                long fileSize = Long.parseLong(parts[2]);
                log.debug("[CONN] DHT search sources for {} with size {}", fileHash, fileSize);
                s.dhtDebugSearch(fileHash, fileSize);
            }
            else if (parts[0].compareTo("dhtsearchkeyword") == 0) {
                for(int i = 1; i < parts.length; ++i) {
                    s.searchDhtKeyword(parts[i], 0, 0, 0, 0);
                }
            }
            else if (parts[0].compareTo("peer") == 0 && parts.length == 3) {
                s.connectToPeer(new Endpoint(Integer.parseInt(parts[1]), (short) Integer.parseInt(parts[2])));
            } else if (parts[0].compareTo("load") == 0 && parts.length == 2) {

                EMuleLink eml = null;
                try {
                    eml = EMuleLink.fromString(parts[1]);
                } catch (JED2KException e ){
                    eml = null;
                }

                if (eml == null) {
                    int index = Integer.parseInt(parts[1]);
                    if (index >= globalSearchRes.size() || index < 0) {
                        log.warn("[CONN] specified index {} out of last search result bounds {}"
                            , index
                            , globalSearchRes.size());
                    } else {
                        SearchEntry sfe = globalSearchRes.get(index);
                        Path filepath = Paths.get(args[0], sfe.getFileName());
                        long filesize = sfe.getFileSize();

                        if (filepath != null && filesize != 0) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("Transfer ").append(filepath).append(" hash: ");
                            sb.append(sfe.getHash().toString()).append(" dataSize: ");
                            sb.append(filesize);
                            handles.add(addTransfer(s, sfe.getHash(), filesize, filepath.toFile()));
                        } else {
                            log.warn("[CONN] not enough parameters to start new transfer");
                        }
                    }
                } else {
                    Path filepath = Paths.get(args[0], eml.getStringValue());
                    handles.add(addTransfer(s, eml.getHash(), eml.getNumberValue(), filepath.toFile()));
                }
            }
            else if (parts[0].compareTo("load") == 0 && parts.length == 4) {
                try {
                    Path filepath = Paths.get(args[0], parts[3]);
                    long size = Long.parseLong(parts[2]);
                    Hash hash = Hash.fromString(parts[1]);
                    log.info("create transfer {} dataSize {} in file {}", hash, size, filepath);
                    handles.add(addTransfer(s, hash, size, filepath.toFile()));
                } catch(Exception e) {
                    log.error("[CONN] unable to start loading {}", e);
                }
            }
            else if (parts[0].compareTo("link") == 0) {
                for(int i = 1; i < parts.length; ++i) {
                    try {
                        EMuleLink link = EMuleLink.fromString(parts[i]);
                        handles.add(addTransfer(s, link.getHash(), link.getNumberValue(), Paths.get(args[0], link.getStringValue()).toFile()));
                    } catch(JED2KException e) {
                        log.error("Unable to parse link {}", e);
                    }
                }
            }
            /*
            else if (parts[0].compareTo("save") == 0) {
                // saving search results to file for next usage
                if (globalSearchRes != null && !globalSearchRes.isEmpty()) {
                    ByteBuffer bb = ByteBuffer.allocate(globalSearchRes.bytesCount());
                    bb.order(ByteOrder.LITTLE_ENDIAN);
                    File f = new File(incomingDirectory.resolve("search_results.txt").toString());

                    try(FileOutputStream stream = new FileOutputStream(f, false);FileChannel channel = stream.getChannel()) {
                        globalSearchRes.put(bb);
                        bb.flip();
                        channel.write(bb);
                        channel.close();
                    } catch(IOException e) {
                        System.out.println("I/O exception on save " + e);
                    } catch(JED2KException e) {
                        System.out.println("Unable to save search result: " + e);
                    }
                } else {
                    System.out.println("Won't save empty search result");
                }
            }
            else if (parts[0].compareTo("restore") == 0) {
                File f = new File(incomingDirectory.resolve("search_results.txt").toString());
                try(FileInputStream stream = new FileInputStream(f); FileChannel channel = stream.getChannel()) {
                    ByteBuffer bb = ByteBuffer.allocate((int)f.length());
                    bb.order(ByteOrder.LITTLE_ENDIAN);
                    channel.read(bb);
                    bb.flip();
                    globalSearchRes = new SearchResult();
                    globalSearchRes.get(bb);
                } catch(IOException e) {
                    System.out.println("I/O exception on load " + e);
                } catch(JED2KException e) {
                    System.out.println("Unable to load search results " + e);
                }
            }
            */
            else if (parts[0].compareTo("print") == 0) {
                printGlobalSearchResult();
            }
            else if ((parts[0].compareTo("delete") == 0) && parts.length == 2) {
                log.debug("delete transfer {}", parts[1]);
                s.removeTransfer(Hash.fromString(parts[1]), true);
            }
            else if (parts[0].compareTo("resume") == 0) {
                File resumeDataFile = resumeDataDirectory.toFile();
                File[] files = resumeDataFile.listFiles();
                ByteBuffer buff = ByteBuffer.allocate(1024);
                buff.order(ByteOrder.LITTLE_ENDIAN);
                for (final File f: files) {
                    try(FileInputStream stream = new FileInputStream(f); FileChannel channel = stream.getChannel()) {
                        channel.read(buff);
                        buff.flip();
                        AddTransferParams atp = new AddTransferParams();
                        atp.get(buff);
                        handles.add(s.addTransfer(atp));
                    }
                    catch(IOException e) {
                        log.error("i/o exception on restore transfer {}", e);
                    }
                    catch(JED2KException e) {
                        log.error("transfer creation error {}", e);
                    } finally {
                        buff.clear();
                    }
                }
            }
            else if (parts[0].compareTo("report") == 0) {
                log.info(report(s));
            }
            else if (parts[0].compareTo("resumetran") == 0 && parts.length == 2) {
                Hash hash = Hash.fromString(parts[1]);
                TransferHandle handle = s.findTransfer(hash);
                if (handle.isValid()) {
                    handle.resume();
                } else {
                    log.warn("transfer {} is not exists", hash);
                }
            }
            else if(parts[0].compareTo("startupnp") == 0) {
                s.startUPnP();
            }
            else if (parts[0].compareTo("stopupnp") == 0) {
                s.stopUPnP();
            }
            else if (parts[0].compareTo("startdht") == 0) {
                if (tracker != null) {
                    log.info("[CONN] stop previously running DHT tracker");
                    tracker.abort();
                }

                tracker = new DhtTracker(GLOBAL_PORT, idata.getTarget(), null);
                tracker.start();
                s.setDhtTracker(tracker);

                if (idata.getEntries().getList() != null) {
                    tracker.addEntries(idata.getEntries().getList());
                }

                scheduledExecutorService.scheduleWithFixedDelay(new Initiator(s), 1, 1, TimeUnit.MINUTES);
            }
            else if (parts[0].compareTo("stopdht") == 0) {

                if (tracker != null) {
                    idata.setEntries(tracker.getTrackerState());
                    tracker.abort();
                } else {
                    log.warn("[CONN] DHT tracker is null, but shtstop command issued");
                }
            }
            else if (parts[0].compareTo("bootstrap") == 0) {
                if (parts.length == 3) {
                    if (tracker != null) {
                        tracker.bootstrap(Collections.singletonList(Endpoint.fromString(parts[1], Integer.parseInt(parts[2]))));
                    } else {
                        log.warn("[CONN] DHT tracker is null, but bootstrap command issued");
                    }
                } else {
                    log.info("[CONN] downloading nodes.dat from inet and bootstrap dht");
                    try {
                        byte[] data = IOUtils.toByteArray(new URI("http://server-met.emulefuture.de/download.php?file=nodes.dat"));
                        ByteBuffer buffer = ByteBuffer.wrap(data);
                        log.debug("[KAD] downloaded nodes.dat size {}", buffer.remaining());
                        buffer.order(ByteOrder.LITTLE_ENDIAN);
                        KadNodesDat nodes = new KadNodesDat();
                        nodes.get(buffer);
                        if (tracker != null) {
                            tracker.addKadEntries(nodes.getContacts());
                            tracker.addKadEntries(nodes.getBootstrapEntries().getList());
                        } else {
                            log.warn("tracker is null");
                        }
                    } catch (Exception e) {
                        log.error("[KAD] unable to initialize DHT from inet: {}", e);
                    }
                }
            }
            else if (parts[0].compareTo("status") == 0) {
                if (tracker != null) {
                    try (PrintWriter pw = new PrintWriter(incomingDirectory.resolve("conn_dht_status.json").toString())) {
                        pw.write(tracker.getRoutingTableStatus());
                    }
                }
            }
            else {
                log.warn("[CONN] unknown command started from {}", parts[0]);
            }
        }

        for(TransferHandle handle: handles) {
            if (handle.isValid()) {
                try {
                    AddTransferParams atp = new AddTransferParams(handle.getHash(), handle.getCreateTime(), handle.getSize(), handle.getFile(), handle.isPaused());
                    atp.resumeData.setData(handle.getResumeData());
                    saveTransferParameters(atp);
                } catch(JED2KException e) {
                    log.error("unable to generate add parameters for {}", handle.getHash());
                }
                log.debug("save resume data for transfer {}", handle.getHash());
            }
        }

        scheduledExecutorService.shutdown();
        FUtils.write(idata, new File(incomingDirectory.resolve(DHT_STATUS_FILENAME).toString()));
        log.info("Conn finished");
    }
}