package org.dkf.jed2k;


import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.PacketCombiner;
import org.dkf.jed2k.protocol.PacketHeader;
import org.dkf.jed2k.protocol.Serializable;
import org.slf4j.Logger;

import java.io.IOException;
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
public class UDPConnection {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(UDPConnection.class);
    private ByteBuffer bufferIncoming;
    private ByteBuffer bufferOutgoing;
    private LinkedList<Pair<Serializable, Endpoint>> outgoingOrder =
            new LinkedList<Pair<Serializable, Endpoint> >();

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
            log.error("[udp] closed channel exception {}", e.getMessage());
        } catch(IOException e) {
            log.error("[udp] i/o exception {}", e.getMessage());
        }
    }

    public void close() {
        try {
            channel.close();
        } catch (IOException e) {
            log.error("[udp] channel close exception {}", e.getMessage());
        }
    }

    public void onReadable() throws JED2KException {
        bufferIncoming.clear();
        try {
            channel.receive(bufferIncoming);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        bufferIncoming.flip();
        // read packet header and body here
        stat.receiveBytes(bufferIncoming.remaining(), 0);
        PacketHeader header = new PacketHeader();
        header.get(bufferIncoming);
        packetCombainer.unpack(header, bufferIncoming);
    }

    public void onWriteable() {
        try {
            bufferOutgoing.clear();
            Pair<Serializable, Endpoint> point = outgoingOrder.poll();

            if (point != null) {
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
