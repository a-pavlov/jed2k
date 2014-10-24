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

import org.jed2k.protocol.Dispatchable;
import org.jed2k.protocol.Dispatcher;
import org.jed2k.protocol.Hash;
import org.jed2k.protocol.LoginRequest;
import org.jed2k.protocol.PacketCombiner;
import org.jed2k.protocol.SearchResult;
import org.jed2k.protocol.ServerIdChange;
import org.jed2k.protocol.ServerInfo;
import org.jed2k.protocol.ServerList;
import org.jed2k.protocol.ServerMessage;
import org.jed2k.protocol.ServerStatus;
import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.Serializable;
import org.jed2k.protocol.ServerGetList;
import org.jed2k.protocol.tag.Tag;

import static org.jed2k.protocol.tag.Tag.tag;
import static org.jed2k.Utils.byte2String;

public class ServerConnection implements Dispatcher {
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
            res.bufferIncoming = ByteBuffer.allocate(4096);
            res.bufferOutgoing = ByteBuffer.allocate(4096);
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
    
    public void onConnectable() {
        try {
            socket.finishConnect();
            write(hello());
            return;
        } catch(IOException e) {            
            log.warning(e.getMessage());            
        } catch(JED2KException e) {
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
    
    public Serializable hello() throws JED2KException {
        LoginRequest login = new LoginRequest();
        int version = 0x3c;
        int capability = LoginRequest.CAPABLE_AUXPORT | LoginRequest.CAPABLE_NEWTAGS | LoginRequest.CAPABLE_UNICODE | LoginRequest.CAPABLE_LARGEFILES;
        int versionClient = (LoginRequest.JED2K_VERSION_MAJOR << 24) | (LoginRequest.JED2K_VERSION_MINOR << 17) | (LoginRequest.JED2K_VERSION_TINY << 10) | (1 << 7);

        login.hash = Hash.EMULE;
        login.point.reset(0, (short)4661);
        
        login.properties.add(tag(Tag.CT_VERSION, null, version));
        login.properties.add(tag(Tag.CT_SERVER_FLAGS, null, capability));
        login.properties.add(tag(Tag.CT_NAME, null, "jed2k"));
        login.properties.add(tag(Tag.CT_EMULE_VERSION, null, versionClient));
        log.info(login.toString());
        return login;
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

    @Override
    public boolean onServerIdChange(ServerIdChange value) {
        log.info("server id changed: " + value);
        return true;
    }

    @Override
    public boolean onServerInfo(ServerInfo value) {
        log.info("server info: " + value);
        return true;
    }

    @Override
    public boolean onServerList(ServerList value) {
        log.info("server list: " + value);
        return true;
    }

    @Override
    public boolean onServerMessage(ServerMessage value) {
        log.info("server message: " + value);
        return true;
    }

    @Override
    public boolean onServerStatus(ServerStatus value) {
        log.info("server status: " + value);
        return true;
    }

    @Override
    public boolean onSearchResult(SearchResult value) {
        log.info("search result: " + value);
        return true;
    }
}