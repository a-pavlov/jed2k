package org.jed2k;


import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;

import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.Dispatchable;
import org.jed2k.protocol.NetworkIdentifier;
import org.jed2k.protocol.PacketCombiner;
import org.jed2k.protocol.PacketHeader;
import org.jed2k.protocol.PacketKey;
import org.jed2k.protocol.Serializable;
import org.jed2k.protocol.ServerPacketCombiner;

/**
 * 
 * @author apavlov
 *
 */
public class UDPConnection {
    private static Logger log = Logger.getLogger(UDPConnection.class.getName());
    private ByteBuffer bufferIncoming;
    private ByteBuffer bufferOutgoing;
    private LinkedList<Pair<Serializable, NetworkIdentifier> > outgoingOrder = 
            new LinkedList<Pair<Serializable, NetworkIdentifier> >();
    private boolean writeInProgress = false;
    private SelectionKey key = null;
    private Statistics stat = new Statistics();
    final Session session;
    DatagramChannel channel;
    private final PacketCombiner packetCombainer = new ServerPacketCombiner();  // temp code
    
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
            Pair<Serializable, NetworkIdentifier> point = outgoingOrder.poll();
            writeInProgress = point != null;
            if (point != null) {
                writeInProgress = true;                
                if (!packetCombainer.pack(point.left, bufferOutgoing)) throw new JED2KException("internal error");
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
            log.warning(e.getMessage());
            assert(false);
        } catch (IOException e) {
            log.warning(e.getMessage());
        }
        
        close();
    }
}
