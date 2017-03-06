package org.dkf.jed2k.kad.server;

import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;

import java.io.IOException;
import java.net.*;

/**
 * Created by apavlov on 06.03.17.
 */
@Slf4j
public class SynDhtTracker {
    private int timeout;
    private int port;
    private DatagramSocket serverSocket = null;

    public SynDhtTracker(int port, int timeout) throws JED2KException {
        this.port = port;
        this.timeout = timeout;
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
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            InetSocketAddress ia = new InetSocketAddress(receivePacket.getAddress(), receivePacket.getPort());
        } catch(SocketTimeoutException e) {
            log.trace("socket timeout");
        } catch (IOException e) {

        }
    }

    public void close() {
        if (serverSocket != null) serverSocket.close();
    }
}
