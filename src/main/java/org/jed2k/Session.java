package org.jed2k;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import org.jed2k.alert.Alert;
import org.jed2k.exception.BaseErrorCode;
import org.jed2k.exception.JED2KException;
import org.jed2k.exception.ErrorCode;
import org.jed2k.protocol.Hash;
import org.jed2k.protocol.NetworkIdentifier;
import org.jed2k.protocol.server.search.SearchRequest;

public class Session extends Thread {
    private static Logger log = Logger.getLogger(Session.class.getName());
    Selector selector = null;
    private ConcurrentLinkedQueue<Runnable> commands = new ConcurrentLinkedQueue<Runnable>();
    ServerConnection serverConection = null;
    private ServerSocketChannel ssc = null;

    Map<Hash, Transfer> transfers = new HashMap<Hash, Transfer>();
    private ArrayList<PeerConnection> connections = new ArrayList<PeerConnection>(); // incoming connections
    Settings settings = new Settings();
    long lastTick = Time.currentTime();

    // from last established server connection
    int clientId    = 0;
    int tcpFlags    = 0;
    int auxPort     = 0;

    private BlockingQueue<Alert> alerts = new LinkedBlockingQueue<Alert>();

    /**
     * start listening server socket
     */
    private void listen() {
        try {
            if (ssc != null) ssc.close();
        }
        catch(IOException e) {
            log.warning("Unable to close server socket channel");
        }

        try {
            log.info("Start listening on " + settings.listenPort);
            ssc = ServerSocketChannel.open();
            ssc.socket().bind(new InetSocketAddress(settings.listenPort));
            ssc.configureBlocking(false);
            ssc.register(selector, SelectionKey.OP_ACCEPT);
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                ssc.close();
                ssc = null;
            } catch(IOException e) {

            }
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
                        log.finest("Key is acceptable");
                        incomingConnection();
                    } else if (key.isConnectable()) {
                        // a connection was established with a remote server/peer.
                        log.finest("Key is connectable");
                        ((Connection)key.attachment()).onConnectable();
                    } else if (key.isReadable()) {
                        // a channel is ready for reading
                        log.finest("Key is readable");
                        ((Connection)key.attachment()).onReadable();
                    } else if (key.isWritable()) {
                        // a channel is ready for writing
                        log.finest("Key is writeable");
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
            secondTick(Time.currentTime());
        }
    }

    public void secondTick(long currentSessionTime) {

        for(Map.Entry<Hash, Transfer> entry : transfers.entrySet()) {
            Hash key = entry.getKey();
            entry.getValue().secondTick(currentSessionTime);
        }

        // second tick on server connection
        if (serverConection != null) serverConection.secondTick(currentSessionTime);

        // TODO - run second tick on peer connections
        // execute user's commands
        Runnable r = commands.poll();
        while(r != null) {
            r.run();
            r = commands.poll();
        }
    }

    @Override
    public void run() {
        // TODO - remove all possible exceptions from this cycle!
        try {
            log.finest("Session started");
            selector = Selector.open();
            listen();

            while(!isInterrupted()) {
                int channelCount = selector.select(1000);
                Time.currentCachedTime = Time.currentTimeHiRes();
                on_tick(ErrorCode.NO_ERROR, channelCount);
            }
        }
        catch(IOException e) {
            log.severe(e.getMessage());
        }
        finally {
            log.info("Session finished");
            try {
                if (selector != null) selector.close();
            }
            catch(IOException e) {

            }
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
            e.printStackTrace();
        }
    }

    void closeConnection(PeerConnection p) {
        connections.remove(p);
    }

    void openConnection(NetworkIdentifier point) throws JED2KException {
        if (findPeerConnection(point) == null) {
            PeerConnection p = PeerConnection.make(Session.this, point, null);
            if (p != null) connections.add(p);
            p.connect();
        }
    }

    public void connectoTo(final InetSocketAddress point) {
        commands.add(new Runnable() {
            @Override
            public void run() {
                if (serverConection != null) {
                    serverConection.close(ErrorCode.NO_ERROR);
                }

                serverConection = ServerConnection.makeConnection(Session.this);
                try {
                    if (serverConection != null) serverConection.connect(point);
                } catch(JED2KException e) {
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

    public void search(final SearchRequest value) {
        commands.add(new Runnable() {
            @Override
            public void run() {
                if (serverConection != null) {
                    serverConection.write(value);
                }
            }
        });
    }

    public void connectToPeer(final NetworkIdentifier point) {
        commands.add(new Runnable() {
            @Override
            public void run() {
                PeerConnection pc = PeerConnection.make(Session.this, point, null);
                if (pc != null) {
                    connections.add(pc);
                    try {
                        pc.connect(point.toInetSocketAddress());
                    } catch(JED2KException e) {
                        log.warning(e.getMessage());
                    }
                } else {
                    log.warning("Unable to create peer connection");
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

    public void pushAlert(Alert e) {
        assert(e != null);
        try {
            alerts.put(e);
        }
        catch (InterruptedException ex) {
            // handle exception
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
    public final synchronized TransferHandle addTransfer(Hash h, long size) {
        Transfer t = transfers.get(h);

        if (t == null) {
            t = new Transfer(this, new AddTransferParams(h, size, "some_filepath"));
            transfers.put(h, t);
        }

        return new TransferHandle(this, t);
    }

    void removeTransfer(Hash h) {
        Transfer t = transfers.get(h);
        transfers.remove(h);
        if (t != null) {
            t.abort();
        }
    }


    void sendSourcesRequest(final Hash h, final long size) {
        if (serverConection != null) serverConection.sendFileSourcesRequest(h, size);
    }
}
