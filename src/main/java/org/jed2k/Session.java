package org.jed2k;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import org.jed2k.alert.Alert;
import org.jed2k.exception.JED2KException;
import org.jed2k.exception.ProtocolCode;
import org.jed2k.protocol.Hash;
import org.jed2k.protocol.NetworkIdentifier;
import org.jed2k.protocol.server.search.SearchRequest;

public class Session extends Thread implements Tickable {
    private static Logger log = Logger.getLogger(Session.class.getName());
    Selector selector = null;
    private ConcurrentLinkedQueue<Runnable> commands = new ConcurrentLinkedQueue<Runnable>();
    private ServerConnection sc = null;
    private ServerSocketChannel ssc = null;

    Map<Hash, Transfer> transfers = new TreeMap<Hash, Transfer>();
    private ArrayList<PeerConnection> connections = new ArrayList<PeerConnection>(); // incoming connections
    private TreeMap<NetworkIdentifier, PeerConnection> downloaders = new TreeMap<NetworkIdentifier, PeerConnection>();
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
    private void listen() throws IOException {
    	if (ssc != null) ssc.close();
    	log.info("Start listening on " + settings.listenPort);
    	ssc = ServerSocketChannel.open();
    	ssc.socket().bind(new InetSocketAddress(settings.listenPort));
    	ssc.configureBlocking(false);
    	ssc.register(selector, SelectionKey.OP_ACCEPT);
    }

    @Override
    public void run() {
        try {
            log.info("Session started");
            selector = Selector.open();
            listen();

            PeerConnection p = null;

            while(!isInterrupted()) {
                int channelCount = selector.select(1000);
                long currentTime = Time.currentTime();
                long tickIntervalMsec = currentTime - lastTick;

                if (channelCount != 0) {
                    // process channels
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                    while(keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();

                        if (key.isValid()) {

                            if(key.isAcceptable()) {
                                // a connection was accepted by a ServerSocketChannel.
                                log.info("Key is acceptable");
                                SocketChannel socket = ssc.accept();
                                p = PeerConnection.make(socket, this);
                            } else if (key.isConnectable()) {
                                // a connection was established with a remote server/peer.
                                log.info("Key is connectable");
                                ((Connection)key.attachment()).onConnectable();
                            } else if (key.isReadable()) {
                                // a channel is ready for reading
                                log.info("Key is readable");
                                ((Connection)key.attachment()).onReadable();
                            } else if (key.isWritable()) {
                                // a channel is ready for writing
                                log.info("Key is writeable");
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
                if (tickIntervalMsec >= 1000) {
                    lastTick = currentTime; // move last tick to current time
                    secondTick(currentTime);
                }
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

    public void connectoTo(final InetSocketAddress point) {
        commands.add(new Runnable() {
            @Override
            public void run() {
                if (sc != null) {
                    sc.close(ProtocolCode.NO_ERROR);
                }

                sc = ServerConnection.makeConnection(Session.this);
                try {
                    if (sc != null) sc.connect(point);
                } catch(JED2KException e) {
                }
            }
        });
    }

    public void disconnectFrom() {
        commands.add(new Runnable() {
            @Override
            public void run() {
                if (sc != null) {
                    sc.close(ProtocolCode.NO_ERROR);
                    sc = null;
                }
            }
        });
    }

    public void search(final SearchRequest value) {
        commands.add(new Runnable() {
            @Override
            public void run() {
                if (sc != null) {
                    sc.write(value);
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

    PeerConnection createPeer(final NetworkIdentifier point, Transfer transfer) {
        if (!downloaders.containsKey(point)) {
            PeerConnection pc = PeerConnection.make(this, point, transfer);
            if (pc != null) {
                downloaders.put(point, pc);
            }
            return pc;
        }

        return null;
    }

    void erasePeer(final NetworkIdentifier point) {
        if (downloaders.containsKey(point))
            downloaders.remove(point);
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
				if (relisten) try {
					listen();
				} catch (IOException e) {
					// TODO - handle this correctly
					log.warning("Unable to listen on " + settings.listenPort);
				}
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

    @Override
    public void secondTick(long tickIntervalMs) {
        // second tick on server connection
        if (sc != null) sc.secondTick(tickIntervalMs);

        // TODO - run second tick on peer connections
        // execute user's commands
        Runnable r = commands.poll();
        while(r != null) {
            r.run();
            r = commands.poll();
        }
    }

    public long getCurrentTime() {
        return lastTick;
    }
}
