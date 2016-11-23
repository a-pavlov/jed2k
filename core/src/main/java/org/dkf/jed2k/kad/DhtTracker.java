package org.dkf.jed2k.kad;

import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.Time;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.hash.MD4;
import org.dkf.jed2k.protocol.*;
import org.dkf.jed2k.protocol.PacketCombiner;
import org.dkf.jed2k.protocol.kad.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.LinkedList;
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
    private long currentTime = Time.currentTimeHiRes();
    private long lastTick = Time.currentTimeHiRes();
    private ByteBuffer incomingBuffer = null;
    private ByteBuffer outgoingBuffer = null;
    private LinkedList<Serializable> outgoingOrder = null;
    private LinkedList<InetSocketAddress> outgoingAddresses = null;
    private PacketCombiner combiner = null;
    private PacketHeader incomingHeader = null;

    public DhtTracker(int listenPort) {
        assert listenPort > 0 && listenPort <= 65535;
        this.listenPort = listenPort;
    }

    @Override
    public void run() {
        try {
            log.debug("DHT tracker start");
            selector = Selector.open();
            channel = DatagramChannel.open();
            channel.socket().bind(new InetSocketAddress(listenPort));
            channel.configureBlocking(false);
            key = channel.register(selector, SelectionKey.OP_READ);
            incomingBuffer = ByteBuffer.allocate(1024);
            outgoingBuffer = ByteBuffer.allocate(1024);
            incomingBuffer.order(ByteOrder.LITTLE_ENDIAN);
            outgoingBuffer.order(ByteOrder.LITTLE_ENDIAN);
            outgoingOrder = new LinkedList<>();
            outgoingAddresses = new LinkedList<>();
            combiner = new org.dkf.jed2k.protocol.kad.PacketCombiner();
            incomingHeader = new KadPacketHeader();

            while(!aborted && !interrupted()) {
                int channelCount = selector.select(1000);
                currentTime = Time.currentTimeHiRes();
                tick(channelCount);
            }
        } catch(IOException e) {
            log.error("I/O exception on DHT starting {}", e.getMessage());
        } finally {
            log.debug("DHT tracker stopping");

            try {
                if (channel != null) channel.close();
            } catch(IOException e) {
                log.error("Datagram channel close error {}", e.getMessage());
            }

            try {
                if (selector != null) selector.close();
            } catch(IOException e) {
                log.error("DHT selector close exception {}", e.getMessage());
            }

            log.debug("DHT tracker finished");
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
        long tickIntervalMs = currentTime - lastTick;
        if (tickIntervalMs >= 1000) {
            lastTick = currentTime;
            Runnable r = commands.poll();
            while(r != null) {
                r.run();
                r = commands.poll();
            }
        }
    }

    private void onReadable() {
        try {
            assert incomingBuffer.remaining() == incomingBuffer.capacity();
            InetSocketAddress address = (InetSocketAddress) channel.receive(incomingBuffer);
            log.debug("receive {} bytes from {}", incomingBuffer.capacity() - incomingBuffer.remaining(), address);
            incomingBuffer.flip();
            incomingHeader.get(incomingBuffer);
            incomingHeader.reset(incomingHeader.key(), incomingBuffer.remaining());
            Serializable t = combiner.unpack(incomingHeader, incomingBuffer);
            assert t != null;
            log.trace("packet {}: {}", t.bytesCount(), t);

        } catch (IOException e) {
            log.error("I/O exception on reading packet {}", incomingHeader);
        } catch (JED2KException e) {
            log.error("exception on parse packet {}", incomingHeader);
        } catch (Exception e) {
            log.error("unexpected error on parse packet {}", incomingHeader);
        } finally {
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    private void onWriteable() {
        if (outgoingOrder.isEmpty()) return;

        Serializable packet = outgoingOrder.poll();
        InetSocketAddress ep = outgoingAddresses.poll();
        assert packet != null;
        outgoingBuffer.clear();
        assert outgoingBuffer.remaining() == outgoingBuffer.capacity();

        try {
            log.debug("send packet size {}", packet.bytesCount());
            combiner.pack(packet, outgoingBuffer);
            outgoingBuffer.flip();
            channel.send(outgoingBuffer, ep);
        }
        catch(JED2KException e) {
            log.error("pack packet {} error {}", packet, e);
        }
        catch (IOException e) {
            log.error("I/O exception on send packet {}", packet);
        } finally {
            // go to wait bytes mode when output order becomes empty
            if (outgoingOrder.isEmpty()) {
                key.interestOps(SelectionKey.OP_READ);
            }
        }
    }

    void write(final Serializable packet, final InetSocketAddress ep) {
        boolean wasInProgress = !outgoingOrder.isEmpty();
        outgoingOrder.add(packet);
        outgoingAddresses.add(ep);

        // writing in progress, no need additional actions here
        if (wasInProgress) return;

        // in writeable case start writing immediately
        // otherwise wait write able state
        if (key.isWritable()) {
            onWriteable();
        } else {
            key.interestOps(SelectionKey.OP_WRITE);
        }
    }

    /**
     * soft stop dht thread
     */
    public void abort() {
        commands.add(new Runnable() {
            @Override
            public void run() {
                aborted = true;
            }
        });
    }

    public synchronized void addNode(final NetworkIdentifier endpoint, final KadId id) {
        node.addNode(endpoint, id);
    }

    public synchronized void bootstrap(final InetSocketAddress ep) {
        write(new Kad2BootstrapReq(), ep);
    }

    public synchronized void searchKey(final InetSocketAddress ep, final String key) {
        MD4 md4 = new MD4();
        md4.update(key.getBytes());
        Hash h = Hash.fromBytes(md4.digest());
        write(Kad2SearchKeysReq.builder().kid(KadId.fromBytes(md4.digest())).startPos(Unsigned.uint16(0)).build(), ep);
    }

    public synchronized void hello(final InetSocketAddress ep) {
        Kad2HelloReq hello = new Kad2HelloReq();
        hello.getKid().assign(Hash.EMULE);
        hello.getVersion().assign(org.dkf.jed2k.protocol.kad.PacketCombiner.KADEMLIA_VERSION5_48a);
        hello.getPortTcp().assign(listenPort);
        write(hello, ep);
    }
}
