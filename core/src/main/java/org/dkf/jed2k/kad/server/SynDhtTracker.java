package org.dkf.jed2k.kad.server;

import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.pool.SynchronizedArrayPool;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;

/**
 * Created by apavlov on 06.03.17.
 */
@Slf4j
public class SynDhtTracker {
    private int timeout;
    private int port;
    private DatagramSocket serverSocket = null;
    private SynchronizedArrayPool packetStorage = new SynchronizedArrayPool(1024, 8096);
    private ExecutorService executor;

    public SynDhtTracker(int port, int timeout, ExecutorService executor) throws JED2KException {
        this.port = port;
        this.timeout = timeout;
        this.executor = executor;

        try {
            serverSocket = new DatagramSocket(port);
            serverSocket.setSoTimeout(timeout);
        } catch(SocketException e) {
            log.error("unable to create udp server socket {}", e);
            throw new JED2KException(ErrorCode.DHT_TRACKER_SOCKET_EXCEPTION);
        }
    }

    public void processPackets() throws JED2KException {
        try {
            byte data[] = packetStorage.allocate();
            if (data != null) {
                DatagramPacket receivePacket = new DatagramPacket(data, data.length);
                serverSocket.receive(receivePacket);
                executor.submit(new DhtRequestHandler(packetStorage
                        , receivePacket));
            } else {
                log.warn("unable to allocate buffer to receive packet");
            }
        } catch(SocketTimeoutException e) {
            log.trace("socket timeout");
        } catch (IOException e) {

        }
    }

    public void close() {
        if (serverSocket != null) serverSocket.close();
    }
}
