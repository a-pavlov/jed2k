package org.jed2k;

import java.io.IOException;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;

import org.jed2k.protocol.Hash;
import org.jed2k.protocol.LoginRequest;
import org.jed2k.protocol.NetworkBuffer;
import org.jed2k.protocol.PacketCombiner;
import org.jed2k.protocol.ProtocolException;
import org.jed2k.protocol.Serializable;
import org.jed2k.protocol.tag.Tag;

import static org.jed2k.protocol.tag.Tag.tag;
import static org.jed2k.Utils.byte2String;

public class ServerConnection {
    private static Logger log = Logger.getLogger(ServerConnection.class.getName());
    private final Session ses;
    private final InetSocketAddress address;
    private SocketChannel socket;
    private ByteBuffer bufferIncoming;
    private ByteBuffer bufferOutgoing;
    private LinkedList<Serializable> outgoingOrder = new LinkedList<Serializable>();
    private boolean writeInProgress = false;
    private SelectionKey key = null;
    private final PacketCombiner packetCombainer = new PacketCombiner();
    
    private ServerConnection(Session ses, final InetSocketAddress address) {
            this.ses = ses;
            this.address = address;
            
    }
    
    public static ServerConnection getServerConnection(Session ses, final InetSocketAddress address) {
        try {
            ServerConnection res = new ServerConnection(ses, address);
            res.bufferIncoming = ByteBuffer.allocate(1024);
            res.bufferOutgoing = ByteBuffer.allocate(1024);
            res.bufferIncoming.order(ByteOrder.LITTLE_ENDIAN);
            res.bufferOutgoing.order(ByteOrder.LITTLE_ENDIAN);
            res.socket = SocketChannel.open();
            res.socket.configureBlocking(false);
            res.key = res.socket.register(ses.selector, SelectionKey.OP_CONNECT, res);
            return res;
        } catch(ClosedChannelException e) {
            
        } catch(IOException e) {
            
        }
        
        return null;
    }
    
    public void readyConnect() {
        try {
            socket.finishConnect();
            write(hello());
            return;
        } catch(IOException e) {            
            log.warning(e.getMessage());            
        } catch(ProtocolException e) {
            log.warning(e.getMessage());            
        }
        
        close();
    }
    
    public void readyRead() {
        try {
            int bytes = socket.read(bufferIncoming);
            if (bytes == -1) {
                close();
                return;
            }
            
            bufferIncoming.flip();
            byte[] rspData = new byte[bytes];
            System.arraycopy(bufferIncoming.array(), 0, rspData, 0, bytes);
            log.info(byte2String(rspData));
            log.info("Read bytes: " + bytes);            
            
            Serializable packet = packetCombainer.unpack(new NetworkBuffer(bufferIncoming));
            if (packet != null) {
                log.info("receive packet");
            }
            // process incoming packet
        } catch(IOException e) {
            log.warning(e.getMessage());
        } catch(ProtocolException e) {
            log.warning(e.getMessage());
        }
        
        close();
    }
    
    public void readyWrite() {
        try {
            writeInProgress = !outgoingOrder.isEmpty();
            Iterator<Serializable> itr = outgoingOrder.iterator();
            while(itr.hasNext()) {
                packetCombainer.pack(itr.next(), new NetworkBuffer(bufferOutgoing));                
                itr.remove();
            }
            
            if (writeInProgress) {
                bufferOutgoing.flip();
                socket.write(bufferOutgoing);
            } else {
                key.interestOps(SelectionKey.OP_READ);
            }
            
            return;
        }
        catch(ProtocolException e) {
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
    
    public Serializable hello() throws ProtocolException {
        LoginRequest login = new LoginRequest();
        int version = 0x3c;
        int capability = LoginRequest.CAPABLE_AUXPORT | LoginRequest.CAPABLE_NEWTAGS | LoginRequest.CAPABLE_UNICODE | LoginRequest.CAPABLE_LARGEFILES;
        int versionClient = (LoginRequest.JED2K_VERSION_MAJOR << 24) | (LoginRequest.JED2K_VERSION_MINOR << 17) | (LoginRequest.JED2K_VERSION_TINY << 10) | (1 << 7);

        login.hash = Hash.EMULE;
        login.point.ip = 0;
        login.point.port = 4661;

        login.properties.add(tag(Tag.CT_VERSION, null, version));
        login.properties.add(tag(Tag.CT_SERVER_FLAGS, null, capability));
        login.properties.add(tag(Tag.CT_NAME, null, "jed2k"));
        login.properties.add(tag(Tag.CT_EMULE_VERSION, null, versionClient));
        log.info(login.toString());
        return login;
    }
       
    public void close() {
        try {
            socket.close();
        } catch(IOException e) {
            log.warning(e.getMessage());
        } finally {
            key.cancel();
        }
    }
    
    public void write(Serializable packet) {
        outgoingOrder.add(packet);
        if (!writeInProgress) {
            key.interestOps(SelectionKey.OP_WRITE);
            readyWrite();
        }
    }
}