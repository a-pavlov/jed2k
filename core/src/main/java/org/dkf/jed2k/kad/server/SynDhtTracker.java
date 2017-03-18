package org.dkf.jed2k.kad.server;

import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.PacketCombiner;
import org.dkf.jed2k.protocol.PacketHeader;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.kad.KadPacketHeader;
import org.postgresql.ds.PGPoolingDataSource;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ExecutorService;

/**
 * Created by apavlov on 06.03.17.
 */
@Slf4j
public class SynDhtTracker extends Thread {
    private int timeout;
    private int port;
    private DatagramSocket serverSocket = null;
    private final ExecutorService executor;
    private final PGPoolingDataSource ds;
    private final byte[] data = new byte[8096];
    private final PacketCombiner combiner = new org.dkf.jed2k.protocol.kad.PacketCombiner();
    private volatile boolean stopFlag = false;

    public SynDhtTracker(int port
            , int timeout
            , ExecutorService executor
            , final PGPoolingDataSource ds) throws JED2KException {
        this.port = port;
        this.timeout = timeout;
        this.executor = executor;
        this.ds = ds;

        log.trace("syn dht tracker started on port {}", port);
        try {
            serverSocket = new DatagramSocket(new InetSocketAddress(InetAddress.getByName("localhost"),port));
            serverSocket.setSoTimeout(timeout);
        } catch(SocketException e) {
            log.error("unable to create udp server socket {}", e);
            throw new JED2KException(ErrorCode.DHT_TRACKER_SOCKET_EXCEPTION);
        } catch(UnknownHostException e) {
            log.error("unable to bind to localhost {}", e);
            throw new JED2KException(ErrorCode.DHT_TRACKER_SOCKET_EXCEPTION);
        }
    }

    public void processPackets() throws JED2KException {
        try {
            DatagramPacket receivePacket = new DatagramPacket(data, data.length);
            serverSocket.receive(receivePacket);
            ByteBuffer buffer = ByteBuffer.wrap(data, 0, receivePacket.getLength());
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            PacketHeader header = new KadPacketHeader();
            header.get(buffer);
            header.reset(header.key(), buffer.remaining());
            log.debug("header {} bytes remaining {}", header, buffer.remaining());
            if (!header.isDefined()) throw new JED2KException(ErrorCode.PACKET_HEADER_UNDEFINED);
            Serializable s = combiner.unpack(header, buffer);
            log.trace("incoming packet {}", s);

            executor.submit(new DhtRequestHandler(s
                        , new InetSocketAddress(receivePacket.getAddress(), receivePacket.getPort())
                        , ds));
        } catch(SocketTimeoutException e) {
            log.trace("socket timeout", e);
            throw new JED2KException(ErrorCode.SOCKET_TIMEOUT);
        } catch (IOException e) {
            log.error("i/o error {}", e);
            throw new JED2KException(ErrorCode.IO_EXCEPTION);
        }
    }

    private void close() {
        if (serverSocket != null) serverSocket.close();
    }

    @Override
    public void run() {
        while(!stopFlag) {
            try {
                processPackets();
            } catch(JED2KException e) {
                log.trace("process packet raised exception {}", e);
            }
        }

        close();
    }

    public void stopTracking() {
        stopFlag = true;
    }
}
