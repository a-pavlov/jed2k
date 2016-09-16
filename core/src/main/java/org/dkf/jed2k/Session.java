package org.dkf.jed2k;

import org.dkf.jed2k.alert.*;
import org.dkf.jed2k.exception.BaseErrorCode;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Hash;
import org.dkf.jed2k.protocol.NetworkIdentifier;
import org.dkf.jed2k.protocol.server.search.SearchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Session extends Thread {
    private static Logger log = LoggerFactory.getLogger(Session.class);
    Selector selector = null;
    private ConcurrentLinkedQueue<Runnable> commands = new ConcurrentLinkedQueue<Runnable>();
    ServerConnection serverConection = null;
    private ServerSocketChannel ssc = null;

    Map<Hash, Transfer> transfers = new HashMap<Hash, Transfer>();
    ArrayList<PeerConnection> connections = new ArrayList<PeerConnection>(); // incoming connections
    Settings settings = null;
    long lastTick = Time.currentTime();
    HashMap<Integer, Hash> callbacks = new HashMap<Integer, Hash>();
    private ByteBuffer skipDataBuffer = null;
    private byte[] zBuffer = null;
    long zBufferLastAllocatedTime = 0;
    BufferPool bufferPool = null;
    private ExecutorService diskIOService = Executors.newSingleThreadExecutor();
    private AtomicBoolean finished = new AtomicBoolean(false);
    private boolean aborted = false;
    private Statistics accumulator = new Statistics();


    // from last established server connection
    int clientId    = 0;
    int tcpFlags    = 0;
    int auxPort     = 0;

    private BlockingQueue<Alert> alerts = new LinkedBlockingQueue<Alert>();

    public Session(final Settings st) {
        // TODO - validate settings before usage
        settings = st;
        bufferPool = new BufferPool(st.bufferPoolSize);
    }

    void closeListenSocket() {
        try {
            if (ssc != null) {
                ssc.close();
            }
        } catch(IOException e) {
            log.error("unable to close listen socket {}", e);
        } finally {
            ssc = null;
        }
    }

    /**
     * start listening server socket
     */
    private void listen() {
        closeListenSocket();

        try {
            if (settings.listenPort > 0) {
                log.info("Start listening on port {}", settings.listenPort);
                ssc = ServerSocketChannel.open();
                ssc.socket().bind(new InetSocketAddress(settings.listenPort));
                ssc.configureBlocking(false);
                ssc.register(selector, SelectionKey.OP_ACCEPT);
                pushAlert(new ListenAlert("", settings.listenPort));
            } else {
                log.info("no listen mode");
            }
        }
        catch(IOException e) {
            log.error("listen failed {}", e.getMessage());
            closeListenSocket();
            pushAlert(new ListenAlert(e.getMessage(), settings.listenPort));
        }
    }

    /**
     * synchronized session internal processing method
     * @param ec
     * @throws IOException
     */
    private synchronized void on_tick(BaseErrorCode ec, int channelCount) {

        if (channelCount != 0) {
            // process channels
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            while(keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();

                if (key.isValid()) {

                    if(key.isAcceptable()) {
                        // a connection was accepted by a ServerSocketChannel.
                        //log.trace("Key is acceptable");
                        incomingConnection();
                    } else if (key.isConnectable()) {
                        // a connection was established with a remote server/peer.
                        //log.trace("Key is connectable");
                        ((Connection)key.attachment()).onConnectable();
                    } else if (key.isReadable()) {
                        // a channel is ready for reading
                        //log.trace("Key is readable");
                        ((Connection)key.attachment()).onReadable();
                    } else if (key.isWritable()) {
                        // a channel is ready for writing
                        //log.trace("Key is writeable");
                        ((Connection)key.attachment()).onWriteable();
                    }
                }

                keyIterator.remove();
            }
        }

        /**
         * handle user's command and process internal tasks in
         * transfers, peers and other structures every 1 second
         */
        long tickIntervalMs = Time.currentTime() - lastTick;
        if (tickIntervalMs >= 1000) {
            lastTick = Time.currentTime();
            secondTick(Time.currentTime(), tickIntervalMs);
        }
    }

    public void secondTick(long currentSessionTime, long tickIntervalMS) {
        for(Map.Entry<Hash, Transfer> entry : transfers.entrySet()) {
            Hash key = entry.getKey();
            entry.getValue().secondTick(accumulator, tickIntervalMS);
        }

        // second tick on server connection
        if (serverConection != null) serverConection.secondTick(tickIntervalMS);

        // TODO - run second tick on peer connections
        // execute user's commands
        Runnable r = commands.poll();
        while(r != null) {
            r.run();
            r = commands.poll();
        }

        accumulator.secondTick(tickIntervalMS);
        connectNewPeers();
        log.trace(bufferPool.toString());
    }

    @Override
    public void run() {
        // TODO - remove all possible exceptions from this cycle!
        try {
            log.debug("Session started");
            selector = Selector.open();
            listen();

            while(!aborted && !interrupted()) {
                int channelCount = selector.select(1000);
                Time.currentCachedTime = Time.currentTimeHiRes();
                on_tick(ErrorCode.NO_ERROR, channelCount);
            }
        }
        catch(IOException e) {
            log.error("session interrupted with error {}", e.getMessage());
        }
        finally {
            log.info("Session is closing");

            try {
                if (selector != null) selector.close();
            }
            catch(IOException e) {
                log.error("close selector failed {}", e.getMessage());
            }

            // close listen socket
            if (ssc != null) {
                try {
                    ssc.close();
                } catch(IOException e) {
                    log.error("listen socket close error {}", e);
                }
            }

            // stop server connection
            if (serverConection != null) serverConection.close(ErrorCode.SESSION_STOPPING);

            // abort all transfers
            for(final Transfer t: transfers.values()) {
                t.abort();
            }

            ArrayList<PeerConnection> localConnections = (ArrayList<PeerConnection>) connections.clone();
            for(final PeerConnection c: localConnections) {
                c.close(ErrorCode.SESSION_STOPPING);
            }

            localConnections.clear();
            connections.clear();

            // stop service
            diskIOService.shutdown();

            log.info("Session finished");
            finished.set(true);

        }
    }

    /**
     * create new peer connection for incoming connection
     */
    void incomingConnection() {
        try {
            SocketChannel sc = ssc.accept();
            PeerConnection p = PeerConnection.make(sc, this);
            connections.add(p);
        }
        catch(IOException e) {
            log.error("Socket accept failed {}", e);
        }
        catch (JED2KException e) {
            log.error("Peer connection creation failed {}", e);
        }
    }

    void closeConnection(PeerConnection p) {
        connections.remove(p);
    }

    void openConnection(NetworkIdentifier point) throws JED2KException {
        if (findPeerConnection(point) == null) {
            PeerConnection p = PeerConnection.make(Session.this, point, null, null);
            if (p != null) connections.add(p);
            p.connect();
        }
    }

    public void connectoTo(final String id, final InetSocketAddress point) {
        commands.add(new Runnable() {
            @Override
            public void run() {
                if (serverConection != null) {
                    serverConection.close(ErrorCode.NO_ERROR);
                }

                try {
                    serverConection = ServerConnection.makeConnection(id, Session.this);
                    serverConection.connect(point);
                    NetworkIdentifier endpoint = new NetworkIdentifier(point);
                    log.debug("connect to server {}", endpoint);
                } catch(JED2KException e) {
                    // emit alert - connect to server failed
                    log.error("server connection failed {}", e);
                }
            }
        });
    }

    public void connectoTo(final String id, final String host, final int port) {
        commands.add(new Runnable() {
            @Override
            public void run() {
                try {
                    final InetSocketAddress addr = new InetSocketAddress(host, port);

                    if (serverConection != null) {
                        serverConection.close(ErrorCode.NO_ERROR);
                    }

                    try {
                        serverConection = ServerConnection.makeConnection(id, Session.this);
                        serverConection.connect(addr);
                        NetworkIdentifier endpoint = new NetworkIdentifier(addr);
                        pushAlert(new ServerConnectionAlert(id));
                        log.debug("connect to server {}", endpoint);
                    } catch(JED2KException e) {
                        // emit alert - connect to server failed
                        log.error("server connection failed {}", e);
                    }
                }
                catch(Exception e) {
                    log.error("Illegal input parameters {} or {}", host, port);
                }
            }
        });
    }

    public void disconnectFrom() {
        commands.add(new Runnable() {
            @Override
            public void run() {
                if (serverConection != null) {
                    serverConection.close(ErrorCode.NO_ERROR);
                }
            }
        });
    }

    synchronized public String getConnectedServerId() {
        if (serverConection != null && serverConection.isHandshakeCompleted()) return serverConection.getIdentifier();
        return "";
    }

    public void search(final SearchRequest value) {
        commands.add(new Runnable() {
            @Override
            public void run() {
                if (serverConection != null) {
                    serverConection.search(value);
                }
            }
        });
    }


    public void searchMore() {
        commands.add(new Runnable() {
            @Override
            public void run() {
                if (serverConection != null) {
                    serverConection.searchMore();
                }
            }
        });
    }

    // TODO - remove only
    public void connectToPeer(final NetworkIdentifier point) {
        commands.add(new Runnable() {
            @Override
            public void run() {
                    try {
                        PeerConnection pc = PeerConnection.make(Session.this, point, null, null);
                        connections.add(pc);
                        pc.connect(point.toInetSocketAddress());
                    } catch(JED2KException e) {
                        log.error("new peer connection failed {}", e);
                    }
            }
        });
    }

    private PeerConnection findPeerConnection(NetworkIdentifier endpoint) {
        for(PeerConnection p: connections) {
            if (p.hasEndpoint() && endpoint.compareTo(p.getEndpoint()) == 0) return p;
        }

        return null;
    }

    /**
     *
     * @param s contains configuration parameters for session
     */
    public void configureSession(final Settings s) {
    	commands.add(new Runnable() {
			@Override
			public void run() {
				boolean relisten = (settings.listenPort != s.listenPort);
				settings = s;
				listen();
			}
    	});
    }

    public void pushAlert(Alert alert) {
        assert(alert != null);
        try {
            alerts.put(alert);
        }
        catch (InterruptedException e) {
            log.error("push alert interrupted {}", e);
        }
    }

    public Alert  popAlert() {
        return alerts.poll();
    }

    public long getCurrentTime() {
        return lastTick;
    }

    /**
     * create new transfer in session or return previous
     * method synchronized with session second tick method
     * @param h hash of file(transfer)
     * @param size of file
     * @return TransferHandle with valid transfer of without
     */
    public final synchronized TransferHandle addTransfer(Hash h, long size, String filepath) throws JED2KException {
        Transfer t = transfers.get(h);

        if (t == null) {
            t = new Transfer(this, new AddTransferParams(h, Time.currentTimeMillis(), size, filepath, false));
            transfers.put(h, t);
        }

        return new TransferHandle(this, t);
    }

    /**
     * create new transfer in session or return previously created transfer
     * using add transfer parameters structure with or without resume data block
     * @param atp transfer parameters with or without resume data
     * @return transfer handle
     * @throws JED2KException
     */
    public final synchronized  TransferHandle addTransfer(final AddTransferParams atp) throws JED2KException {
        Transfer t = transfers.get(atp.hash);

        if (t == null) {
            t = new Transfer(this, atp);
            transfers.put(atp.hash, t);
        }

        return new TransferHandle(this, t);
    }

    public final synchronized TransferHandle findTransfer(final Hash h) {
        return new TransferHandle(this, transfers.get(h));
    }

    public void removeTransfer(final Hash h, final boolean removeFile) {
        commands.add(new Runnable() {
            @Override
            public void run() {
                    Transfer t = transfers.get(h);
                    if (t != null) {
                        // add delete file here
                        t.abort();
                        if (removeFile) t.deleteFile();
                        transfers.remove(h);
                        pushAlert(new TransferRemovedAlert(h));
                    }
            }
        });
    }

    public final synchronized List<TransferHandle> getTransfers() {
        LinkedList<TransferHandle> handles = new LinkedList<TransferHandle>();
        for(final Transfer t: transfers.values()) {
            /*if (!t.isAborted())*/ handles.add(new TransferHandle(this, t));
        }

        return handles;
    }

    void sendSourcesRequest(final Hash h, final long size) {
        if (serverConection != null) serverConection.sendFileSourcesRequest(h, size);
    }

    /**
     * executes each second
     * traverse transfers list and try to connect new peers if limits not exceeded and transfer want more peers
     */
    void connectNewPeers() {
        int stepsSinceLastConnect = 0;
        int maxConnectionsPerSecond = settings.maxConnectionsPerSecond;
        int numTransfers = transfers.size();
        boolean enumerateCandidates = true;

        if (numTransfers > 0 && connections.size() < settings.sessionConnectionsLimit) {
            //log.finest("connectNewPeers with transfers count " + numTransfers);
            while (enumerateCandidates) {
                for (Map.Entry<Hash, Transfer> entry : transfers.entrySet()) {
                    Hash key = entry.getKey();
                    Transfer t = entry.getValue();

                    if (t.wantMorePeers()) {
                        try {
                            if (t.tryConnectPeer(Time.currentTime())) {
                                --maxConnectionsPerSecond;
                                stepsSinceLastConnect = 0;
                            }
                        } catch (JED2KException e) {
                            log.error("exception on connect new peer {}", e);
                        }
                    }

                    ++stepsSinceLastConnect;

                    // if we have gone two whole loops without
                    // handing out a single connection, break
                    if (stepsSinceLastConnect > numTransfers*2) {
                        enumerateCandidates = false;
                        break;
                    }

                    // if we should not make any more connections
                    // attempts this tick, abort
                    if (maxConnectionsPerSecond == 0) {
                        enumerateCandidates = false;
                        break;
                    }
                }

                // must not happen :) but still
                if (transfers.isEmpty()) break;
            }
        }
    }

    /**
     * allocate new fixed size byte buffer from session's buffer pool
     * @return byte buffer from common session buffer pool
     */
    public ByteBuffer allocatePoolBuffer() {
        return bufferPool.allocate();
    }

    /**
     * sometimes we need to skip some data received from peer
     * skip data buffer is one shared data buffer for all connections
     * @return byte buffer
     */
    ByteBuffer allocateSkipDataBufer() {
        if (skipDataBuffer == null) {
            skipDataBuffer = ByteBuffer.allocate(Constants.BLOCK_SIZE_INT);
        }

        return skipDataBuffer.duplicate();
    }

    /**
     * provide one per session temporary buffer for inflate z data
     * @return common z buffer for decompress compressed data
     */
    byte[] allocateTemporaryInflateBuffer() {
        zBufferLastAllocatedTime = Time.currentTime();
        if (zBuffer == null) zBuffer = new byte[Constants.BLOCK_SIZE_INT];
        return zBuffer;
    }

    /**
     * execute async disk operation
     * @param task special task
     * @return future
     */
    public Future<AsyncOperationResult> submitDiskTask(Callable<AsyncOperationResult> task) {
        return diskIOService.submit(task);
    }

    @Override
    public String toString() {
        return "Session";
    }

    public Hash getUserAgent() { return settings.userAgent; }
    public int getClientId() { return clientId; }
    public int getListenPort() { return settings.listenPort; }
    public String getClientName() { return settings.clientName; }
    public String getModName() { return settings.modName; }
    public int getAppVersion() { return settings.version; }
    public int getCompressionVersion() { return settings.compressionVersion; }
    public int getModMajorVersion() { return settings.modMajor; }
    public int getModMinorVersion() { return settings.modMinor; }
    public int getModBuildVersion() { return settings.modBuild; }

    /**
     * thread safe
     * @return status of session
     */
    public final boolean isFinished() {
        return finished.get();
    }

    /**
     * stop main session cycle
     * guarantees all previous commands were completed
     */
    public void abort() {
        commands.add(new Runnable() {
            @Override
            public void run() {
                aborted = true;
            }
        });
    }

    /**
     * save resume data on all transfers needs to save resume data
     */
    public void saveResumeData() {
        commands.add(new Runnable() {
            @Override
            public void run() {
                for(final Transfer t: transfers.values()) {
                    if (t.isNeedSaveResumeData()) {
                        try {
                            AddTransferParams atp = new AddTransferParams(t.hash(), t.getCreateTime(), t.size(), t.getFilePath().getAbsolutePath(), t.isPaused());
                            atp.resumeData.setData(t.resumeData());
                            pushAlert(new TransferResumeDataAlert(t.hash(), atp));
                        } catch(JED2KException e) {
                            log.error("prepare resume data for {} failed {}", t.hash(), e);
                        }
                    }
                }
            }
        });
    }

    synchronized  public Pair<Long, Long> getDownloadUploadRate() {
        long dr = accumulator.downloadRate() + accumulator.downloadPayloadRate();
        long ur = accumulator.uploadRate() + accumulator.uploadPayloadRate();
        return Pair.make(dr, ur);
    }
}
