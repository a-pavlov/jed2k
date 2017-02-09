package org.dkf.jed2k;


import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.PacketCombiner;
import org.dkf.jed2k.protocol.PacketHeader;
import org.dkf.jed2k.protocol.Serializable;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.util.LinkedList;

/**
 *
 * @author apavlov
 *
 */
@Slf4j
public class UDPConnection {
    private ByteBuffer bufferIncoming;
    private ByteBuffer bufferOutgoing;
    private LinkedList<Pair<Serializable, Endpoint>> outgoingOrder =
            new LinkedList<Pair<Serializable, Endpoint> >();
    private boolean writeInProgress = false;
    private SelectionKey key = null;
    private Statistics stat = new Statistics();
    final Session session;
    DatagramChannel channel;
    private final PacketCombiner packetCombainer = new org.dkf.jed2k.protocol.server.PacketCombiner();  // temp code

    public UDPConnection(final Session session) {
        this.session = session;
        try {
            bufferIncoming = ByteBuffer.allocate(4096);
            bufferOutgoing = ByteBuffer.allocate(4096);
            channel = DatagramChannel.open();
            channel.configureBlocking(false);
            key = channel.register(session.selector, SelectionKey.OP_READ, this);
        } catch(ClosedChannelException e) {

        } catch(IOException e) {

        }
    }

    public void close() {
        try {
            channel.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void onReadable() throws JED2KException {
        bufferIncoming.clear();
        try {
            SocketAddress addr = channel.receive(bufferIncoming);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        bufferIncoming.flip();
        // read packet header and body here
        stat.receiveBytes(bufferIncoming.remaining(), 0);
        PacketHeader header = new PacketHeader();
        header.get(bufferIncoming);
        Serializable packet = packetCombainer.unpack(header, bufferIncoming);
        // send packet to dispather
        // Serializable packet = packetCombainer.unpack(header, bufferIncoming);
    }

    public void onWriteable() {
        try {
            bufferOutgoing.clear();
            Pair<Serializable, Endpoint> point = outgoingOrder.poll();
            writeInProgress = point != null;
            if (point != null) {
                writeInProgress = true;
                if (!packetCombainer.pack(point.left, bufferOutgoing)) throw new JED2KException(ErrorCode.FAIL);
                bufferOutgoing.flip();
                stat.sendBytes(bufferOutgoing.remaining(), 0);
                channel.write(bufferOutgoing);
            }
            else {
                key.interestOps(SelectionKey.OP_READ);
            }

            return;
        }
        catch(JED2KException e) {
            log.warn("[udp writeable] jed2k error {}", e);
            assert(false);
        } catch (IOException e) {
            log.warn("[udp writeable] i/o error {}", e);
        }

        close();
    }
}
