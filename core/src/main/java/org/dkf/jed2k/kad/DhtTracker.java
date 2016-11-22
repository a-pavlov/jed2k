package org.dkf.jed2k.kad;

import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.Connection;
import org.dkf.jed2k.Time;
import org.dkf.jed2k.protocol.Serializable;

import java.io.IOException;
import java.net.InetSocketAddress;
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
    private long currentTime = Time.currentTimeHiRes();
    private long lastTick = Time.currentTimeHiRes();
    private ByteBuffer incomingBuffer = null;
    private ByteBuffer outgoingBuffer = null;
    private LinkedList<Serializable> outgoingOrder = null;

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

    private void tick(int channelCount) {
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
            // check buffer is empty
            assert incomingBuffer.hasRemaining();
            assert incomingBuffer.remaining() == incomingBuffer.capacity();
            InetSocketAddress address = (InetSocketAddress) channel.receive(incomingBuffer);
            incomingBuffer.flip();
            // deserialize packet and deliver it to recipients
            key.interestOps(SelectionKey.OP_READ);
        } catch (IOException e) {

        } catch (Exception e) {

        }
    }

    private void onWriteable() {
        if (outgoingOrder.isEmpty()) return;

        Serializable packet = outgoingOrder.peek();
        assert packet != null;
        outgoingBuffer.flip();
        assert outgoingBuffer.remaining() == outgoingBuffer.capacity();
        // serialize packet here
        try {
            channel.send(outgoingBuffer, new InetSocketAddress(4444));
        } catch (IOException e) {

        }

        if (outgoingOrder.isEmpty()) {
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    void write(final Serializable packet) {
        boolean wasInProgress = !outgoingOrder.isEmpty();
        outgoingOrder.add(packet);

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
}
