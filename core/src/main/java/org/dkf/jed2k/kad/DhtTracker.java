package org.dkf.jed2k.kad;

import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.Pair;
import org.dkf.jed2k.Time;
import org.dkf.jed2k.Utils;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.hash.MD4;
import org.dkf.jed2k.protocol.*;
import org.dkf.jed2k.protocol.PacketCombiner;
import org.dkf.jed2k.protocol.kad.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by inkpot on 21.11.2016.
 */
@Slf4j
public class DhtTracker extends Thread {
    private NodeImpl node;
    private ConcurrentLinkedQueue<Runnable> commands = new ConcurrentLinkedQueue<Runnable>();
    private Selector selector = null;
    private DatagramChannel channel = null;
    private SelectionKey key = null;
    private int listenPort;
    private boolean aborted = false;
    private long lastTick = Time.currentTimeHiRes();
    private ByteBuffer incomingBuffer = null;
    private ByteBuffer outgoingBuffer = null;
    private LinkedList<Serializable> outgoingOrder = null;
    private LinkedList<InetSocketAddress> outgoingAddresses = null;
    private PacketCombiner combiner = null;
    private PacketHeader incomingHeader = null;
    private int localAddress = 0;

    private static int OUTPUT_BUFFER_LIMIT = 8128;
    private static int INPUT_BUFFER_LIMIT = 8128;

    public DhtTracker(int listenPort, final KadId id) {
        assert listenPort > 0 && listenPort <= 65535;
        this.listenPort = listenPort;
        this.node = new NodeImpl(this, id, listenPort);
    }

    @Override
    public void run() {
        // TODO - remove this incorrect code for obtain our address since it won't work on more than one interfaces
        String host = "";
        try {
            host = InetAddress.getLocalHost().getHostAddress();
            localAddress = Utils.string2Ip(host);
        }
        catch (UnknownHostException e) {
            log.error("[tracker] unknown host exception");
        }
        catch(JED2KException e) {
            log.error("[tracker] unable to parse host {} {}", host, e);
        }

        log.debug("[tracker] local host {}", Utils.ip2String(localAddress));
        node.setAddress(localAddress);

        try {
            InetSocketAddress addr = new InetSocketAddress(listenPort);
            log.debug("[tracker] starting {}", addr.getAddress().getHostAddress());
            selector = Selector.open();
            channel = DatagramChannel.open();
            channel.socket().bind(addr);
            channel.configureBlocking(false);
            key = channel.register(selector, SelectionKey.OP_READ);
            incomingBuffer = ByteBuffer.allocate(INPUT_BUFFER_LIMIT);
            outgoingBuffer = ByteBuffer.allocate(OUTPUT_BUFFER_LIMIT);
            incomingBuffer.order(ByteOrder.LITTLE_ENDIAN);
            outgoingBuffer.order(ByteOrder.LITTLE_ENDIAN);
            outgoingOrder = new LinkedList<>();
            outgoingAddresses = new LinkedList<>();
            combiner = new org.dkf.jed2k.protocol.kad.PacketCombiner();
            incomingHeader = new KadPacketHeader();

            while (!aborted && !interrupted()) {
                int channelCount = selector.select(1000);
                // TODO - do not update global time here when we work in main session scope
                Time.updateCachedTime();
                tick(channelCount);
            }
        } catch (IOException e) {
            log.error("[tracker] I/O exception on DHT starting {}", e);
        } catch (Exception e) {
            log.error("[tracker] unexpected error {}", e);
            e.printStackTrace();
        }
        finally {
            log.debug("[tracker] stopping");

            try {
                if (channel != null) channel.close();
            } catch(IOException e) {
                log.error("[tracker] datagram channel close error {}", e);
            }

            try {
                if (selector != null) selector.close();
            } catch(IOException e) {
                log.error("[tracker] selector close exception {}", e);
            }

            node.abort();

            log.debug("[tracker] tracker finished");
        }
    }

    synchronized private void tick(int channelCount) {
        if (channelCount != 0) {
            assert selector != null;
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();

                if (key.isValid()) {
                    if (key.isReadable()) {
                        onReadable();
                    } else if (key.isWritable()) {
                        onWriteable();
                    }
                }

                keyIterator.remove();
            }
        }

        // process user's command every second
        long tickIntervalMs = Time.currentTime() - lastTick;
        if (tickIntervalMs >= 1000) {
            lastTick = Time.currentTime();
            Runnable r = commands.poll();
            while(r != null) {
                r.run();
                r = commands.poll();
            }

            node.tick();
        }
    }

    public int getOutputBufferLimit() {
        return OUTPUT_BUFFER_LIMIT;
    }

    public int getInputBufferLimit() {
        return INPUT_BUFFER_LIMIT;
    }

    private void onReadable() {
        try {
            assert incomingBuffer.remaining() == incomingBuffer.capacity();
            InetSocketAddress address = (InetSocketAddress) channel.receive(incomingBuffer);
            log.debug("[tracker] receive {} bytes from {}", incomingBuffer.capacity() - incomingBuffer.remaining(), address);
            incomingBuffer.flip();
            incomingHeader.get(incomingBuffer);
            if (!incomingHeader.isDefined()) throw new JED2KException(ErrorCode.PACKET_HEADER_UNDEFINED);

            incomingHeader.reset(incomingHeader.key(), incomingBuffer.remaining());
            Serializable s = combiner.unpack(incomingHeader, incomingBuffer);
            assert s != null;
            log.debug("[tracker] packet {}: {}", s.bytesCount(), s);
            node.incoming(s, address);
        } catch (IOException e) {
            log.error("[tracker] I/O exception {} on reading packet {}", e, incomingHeader);
            e.printStackTrace();
        } catch (JED2KException e) {
            e.printStackTrace();
            log.error("[tracker] exception {} on parse packet {}", e, incomingHeader);
            //log.error("packet dump \n{}", HexDump.dump(incomingBuffer.array()));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("[tracker] unexpected error on parse packet {} {}", incomingHeader, e);
            //log.error("packet dump \n{}", HexDump.dump(incomingBuffer.array()));
        } finally {
            incomingBuffer.clear();

            if (outgoingOrder.isEmpty()) {
                log.debug("[tracker] set interests to OP_READ since outgoing order is empty");
                key.interestOps(SelectionKey.OP_READ);
            }
        }
    }

    private boolean onWriteable() {
        log.debug("[tracker] onWriteable, order size {}", outgoingOrder.size());
        if (outgoingOrder.isEmpty()) return false;

        Serializable packet = outgoingOrder.poll();
        InetSocketAddress ep = outgoingAddresses.poll();
        assert packet != null;
        outgoingBuffer.clear();
        assert outgoingBuffer.remaining() == outgoingBuffer.capacity();

        try {
            log.debug("[tracker] send packet size {}", packet.bytesCount());
            combiner.pack(packet, outgoingBuffer);
            outgoingBuffer.flip();
            channel.send(outgoingBuffer, ep);
            return true;
        }
        catch(JED2KException e) {
            log.error("[tracker] pack packet {} error {}", packet, e);
        }
        catch (IOException e) {
            log.error("[tracker] I/O exception on send packet {}", packet);
        }
        catch(Exception e) {
            log.error("[tracker] unexpected error {}", e);
            e.printStackTrace();
        }
        finally {
            // go to wait bytes mode when output order becomes empty
            if (outgoingOrder.isEmpty()) {
                log.debug("[tracker] set interests to OP_READ");
                key.interestOps(SelectionKey.OP_READ);
            }
        }

        return false;
    }

    /**
     *
     * @param packet data block
     * @param ep target endpoint
     * @return true if packet was written to socket and in case of deferred sending
     * expect this method will write data immediately - need to be verified
     */
    public boolean write(final Serializable packet, final InetSocketAddress ep) {
        boolean wasInProgress = !outgoingOrder.isEmpty();
        log.debug("[tracker] write was in progress {}", wasInProgress);
        outgoingOrder.add(packet);
        outgoingAddresses.add(ep);

        // writing in progress, no need additional actions here
        if (wasInProgress) return true;

        // in writeable case start writing immediately
        // otherwise wait write able state
        if (key.isWritable()) {
            // return actual write result
            boolean res = onWriteable();
            log.debug("[tracker] actual write to {} is {}", ep, res);
        } else {
            log.debug("[tracker] set interests to OP_WRITE");
            key.interestOps(SelectionKey.OP_WRITE);
        }

        return true;
    }

    /**
     * soft stop dht thread
     */
    public synchronized void abort() {
        if (aborted) return;
        aborted = true;
    }

    public synchronized boolean isAborted() {
        return aborted;
    }

    public synchronized void addNode(final Endpoint endpoint, final KadId id) throws JED2KException {
        node.addNode(endpoint, id);
    }

    /**
     * adds initial nodes
     * @param entries
     */
    public void addEntries(final List<NodeEntry> entries) {
        assert entries != null;
        commands.add(new Runnable() {
            @Override
            public void run() {
                for(final NodeEntry e: entries) {
                    try {
                        node.addNode(e.getEndpoint(), e.getId());
                    } catch(JED2KException ex) {
                        log.error("[tracker] unable to add node {} due to error {}", e, ex);
                    }
                }
            }
        });
    }

    /**
     * add initial nodes
     * @param entries - KAD entries from nodes.dat file usually
     */
    public void addKadEntries(final List<KadEntry> entries) {
        assert entries != null;
        commands.add(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                for(final KadEntry e: entries) {
                    try {
                        node.addKadNode(e);
                        ++i;
                        //if (i > 100) break;
                    } catch(JED2KException ex) {
                        log.error("[tracker] unable to add kad node {} due to error {}", e, ex);
                    }
                }
            }
        });
    }

    public synchronized void bootstrapTest(final InetSocketAddress ep) {
        write(new Kad2BootstrapReq(), ep);
    }

    public synchronized void searchKeywords(final String key, final Listener l) throws JED2KException {
        log.debug("[tracker] search keyword {}", key);
        MD4 md4 = new MD4();
        md4.update(key.getBytes());
        Kad2SearchKeysReq req = new Kad2SearchKeysReq();
        node.searchKeywords(KadId.fromBytes(md4.digest()), l);
    }

    /**
     * search file sources by hash
     * if tracker aborted simply returns empty list
     * @param hash of file
     * @param fileSize size of file
     * @param l callback interface
     * @throws JED2KException when problems occurred during start
     */
    public synchronized void searchSources(final Hash hash, final long fileSize, final Listener l) throws JED2KException {
        if (aborted) l.process(new LinkedList<KadSearchEntry>());
        else node.searchSources(new KadId(hash), fileSize, l);
    }

    public synchronized void hello(final InetSocketAddress ep) {
        Kad2HelloReq hello = new Kad2HelloReq();
        hello.getKid().assign(Hash.EMULE);
        hello.getVersion().assign(org.dkf.jed2k.protocol.kad.PacketCombiner.KADEMLIA_VERSION);
        hello.getPortTcp().assign(listenPort);
        write(hello, ep);
    }

    public synchronized void bootstrap(final List<Endpoint> endpoints) throws JED2KException {
        node.bootstrap(endpoints);
    }

    /**
     * just for test firewalled request/response
     * @throws JED2KException
     */
    public synchronized void firewalled() throws JED2KException {
        node.firewalled();
    }

    public void status() {
        commands.add(new Runnable() {
            @Override
            public void run() {
                node.logStatus();
            }
        });
    }

    public synchronized String getRoutingTableStatus() {
        return node.getRoutingTableStatus();
    }

    /**
     *
     * @return current routing table status
     */
    public synchronized Container<UInt32, NodeEntry> getTrackerState() {
        DhtState stateCollector = new DhtState();
        node.getTable().forEach(stateCollector);
        return stateCollector.getEntries();
    }

    /**
     *
     * @return routing table size of alive and replacements nodes
     */
    public synchronized Pair<Integer, Integer> getRoutingTableSize() {
        return node.getTable().getSize();
    }

    public synchronized boolean needBootstrap() {
        return node.getTable().needBootstrap();
    }

    public synchronized boolean isFirewalled() {
        return node.isFirewalled();
    }
}
