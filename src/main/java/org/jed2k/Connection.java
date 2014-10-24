package org.jed2k;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;

import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.Dispatchable;
import org.jed2k.protocol.Dispatcher;
import org.jed2k.protocol.PacketCombiner;
import org.jed2k.protocol.Serializable;

public abstract class Connection implements Dispatcher {
    private static Logger log = Logger.getLogger(ServerConnection.class.getName());
    private final InetSocketAddress address;
    private SocketChannel socket;
    private ByteBuffer bufferIncoming;
    private ByteBuffer bufferOutgoing;
    private LinkedList<Serializable> outgoingOrder = new LinkedList<Serializable>();
    private boolean writeInProgress = false;
    private SelectionKey key = null;
    private final PacketCombiner packetCombainer = new PacketCombiner();
    
    protected Connection(Session ses, 
            InetSocketAddress address, 
            ByteBuffer bufferIncoming,
            ByteBuffer bufferOutgoing) throws IOException {
        this.address = address;        
        this.bufferIncoming = bufferIncoming;
        this.bufferOutgoing = bufferOutgoing;
        this.bufferIncoming.order(ByteOrder.LITTLE_ENDIAN);
        this.bufferOutgoing.order(ByteOrder.LITTLE_ENDIAN);       
        socket = SocketChannel.open();
        socket.configureBlocking(false);
        key = socket.register(ses.selector, SelectionKey.OP_CONNECT, this);
    }
       
    public void onConnectable() {
        try {
            socket.finishConnect();
            return;
        } catch(IOException e) {            
            log.warning(e.getMessage());            
        }
        
        close();
    }
    
    public void onReadable() {
        try {
            int bytes = socket.read(bufferIncoming);
            log.info("ready to read byte: " + bytes);
            if (bytes == -1) {
                close();
                return;
            }
            
            bufferIncoming.flip();
            
            while(true) {                
                Serializable packet = packetCombainer.unpack(bufferIncoming);                
                
                if (packet != null && (packet instanceof Dispatchable)) {
                    // packet was completely in buffer
                    ((Dispatchable)packet).dispatch(this);
                } else {
                    // buffer too low, try to compact it and read again
                    bufferIncoming.compact();
                    break;
                }
            }
            return;
        } catch(IOException e) {
            log.warning(e.getMessage());
        } catch(JED2KException e) {
            log.warning(e.getMessage());
        }
        
        close();
    }
    
    public void onWriteable() {
        try {
            bufferOutgoing.clear();
            writeInProgress = !outgoingOrder.isEmpty();
            Iterator<Serializable> itr = outgoingOrder.iterator();
            while(itr.hasNext()) {
                packetCombainer.pack(itr.next(), bufferOutgoing);
                itr.remove();
            }
            
            if (writeInProgress) {
                bufferOutgoing.flip();
                socket.write(bufferOutgoing);
            } else {
                key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
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
    
    public void connect() {    
        try {
            socket.connect(address);
        } catch(IOException e) {
           log.warning(e.getMessage());
           close();
        }
    }
    
    public void close() {
        log.info("close socket");
        try {
            socket.close();
        } catch(IOException e) {
            log.warning(e.getMessage());
        } finally {
            key.cancel();
        }
    }
    
    public void write(Serializable packet) {
        log.info("write packet " + packet);
        outgoingOrder.add(packet);
        if (!writeInProgress) {
            key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
            onWriteable();
        }
    }
}
