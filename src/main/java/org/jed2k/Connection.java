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
import org.jed2k.protocol.NetworkIdentifier;
import org.jed2k.protocol.PacketCombiner;
import org.jed2k.protocol.PacketHeader;
import org.jed2k.protocol.Serializable;

public abstract class Connection implements Dispatcher {
    private static Logger log = Logger.getLogger(ServerConnection.class.getName());
    private SocketChannel socket;
    private ByteBuffer bufferIncoming;
    private ByteBuffer bufferOutgoing;
    private LinkedList<Serializable> outgoingOrder = new LinkedList<Serializable>();
    private boolean writeInProgress = false;
    private SelectionKey key = null;
    private final PacketCombiner packetCombainer;
    final Session session;
    private long totalBytesIncoming = 0;
    private long totalBytesOutgoing = 0;
    private long birthdayTime = 0;  // in milliseconds
    private long incomingSpeed = 0; // bytes per second
    private PacketHeader header = new PacketHeader();
    private ByteBuffer headerBuffer = ByteBuffer.allocate(PacketHeader.SIZE);
    
    protected Connection(ByteBuffer bufferIncoming,
            ByteBuffer bufferOutgoing,
            PacketCombiner packetCombiner,
            Session session) throws IOException {
        this.bufferIncoming = bufferIncoming;
        this.bufferOutgoing = bufferOutgoing;
        this.bufferIncoming.order(ByteOrder.LITTLE_ENDIAN);
        this.bufferOutgoing.order(ByteOrder.LITTLE_ENDIAN);
        this.headerBuffer.order(ByteOrder.LITTLE_ENDIAN);
        this.packetCombainer = packetCombiner;
        this.session = session;
        socket = SocketChannel.open();
        socket.configureBlocking(false);
        key = socket.register(session.selector, SelectionKey.OP_CONNECT, this);
    }
    
    protected Connection(ByteBuffer bufferIncoming,
            ByteBuffer bufferOutgoing, 
            PacketCombiner packetCombiner,
            Session session, SocketChannel socket) throws IOException {        
        this.bufferIncoming = bufferIncoming;
        this.bufferOutgoing = bufferOutgoing;
        this.bufferIncoming.order(ByteOrder.LITTLE_ENDIAN);
        this.bufferOutgoing.order(ByteOrder.LITTLE_ENDIAN);
        this.headerBuffer.order(ByteOrder.LITTLE_ENDIAN);
        this.headerBuffer.order(ByteOrder.LITTLE_ENDIAN);
        this.packetCombainer = packetCombiner;
        this.session = session;
        this.socket = socket;
        this.socket.configureBlocking(false);
        key = socket.register(session.selector, SelectionKey.OP_READ, this);
    }
    
    public void onConnectable() {
        try {
            socket.finishConnect();
            onConnect();
            birthdayTime = System.nanoTime() / 1000000;    // milliseconds from system timer 
            return;
        } catch(IOException e) {            
            log.warning(e.getMessage());            
        } catch(JED2KException e) {
            log.warning(e.getMessage());
        }
        
        close();
    }
    
    protected void processHeader() throws IOException, JED2KException {
        assert(!header.isDefined());
        assert(headerBuffer.remaining() != 0);
        
        int bytes = socket.read(headerBuffer);
        if (bytes == -1) throw new JED2KException("processHeader: End of stream");
        
        if (headerBuffer.remaining() == 0) {
            headerBuffer.flip();
            assert(headerBuffer.remaining() == PacketHeader.SIZE);
            header.get(headerBuffer);
            headerBuffer.clear();
            log.info("processHeader:" + header.toString());
            bufferIncoming.limit(PacketCombiner.serviceSize(header));
        }
    }
    
    protected void processBody() throws IOException, JED2KException {
        if(bufferIncoming.remaining() != 0) {
            int bytes = socket.read(bufferIncoming);
            if (bytes == -1) throw new JED2KException("processBody: End of stream");
        }
        
        if (bufferIncoming.remaining() == 0) {
            bufferIncoming.flip();
            Serializable packet = packetCombainer.unpack(header, bufferIncoming);
            bufferIncoming.clear();
            header.reset();
            if (packet != null && (packet instanceof Dispatchable)) {
                ((Dispatchable)packet).dispatch(this);
            }
        }
    }
    
    public void onReadable() {
        try {
            if (!header.isDefined()) {
                processHeader();
            } else {
                processBody();
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
                if (!packetCombainer.pack(itr.next(), bufferOutgoing)) break;
                itr.remove();
            }
            
            if (writeInProgress) {
                bufferOutgoing.flip();
                socket.write(bufferOutgoing);
                totalBytesOutgoing += bufferOutgoing.remaining();
            } else {
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
    
    protected abstract void onConnect() throws JED2KException;
    protected abstract void onDisconnect();
    
    public void connect(final InetSocketAddress address) throws JED2KException {    
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
            onDisconnect();
            key.cancel();
        }
    }
    
    public void write(Serializable packet) {
        log.info("write packet " + packet);
        outgoingOrder.add(packet);
        if (!writeInProgress) {
            key.interestOps(SelectionKey.OP_WRITE);
            onWriteable();
        }
    }
    
    public void secondTick(long mSeconds) {
        long duration = mSeconds - birthdayTime;
        if (duration != 0) {
            incomingSpeed = totalBytesIncoming*1000 / duration;
        }
    }
}
