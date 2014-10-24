package org.jed2k;

import java.io.IOException;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.logging.Logger;

import org.jed2k.protocol.ClientExtHello;
import org.jed2k.protocol.ClientExtHelloAnswer;
import org.jed2k.protocol.ClientHello;
import org.jed2k.protocol.ClientHelloAnswer;
import org.jed2k.protocol.Hash;
import org.jed2k.protocol.LoginRequest;
import org.jed2k.protocol.SearchResult;
import org.jed2k.protocol.ServerIdChange;
import org.jed2k.protocol.ServerInfo;
import org.jed2k.protocol.ServerList;
import org.jed2k.protocol.ServerMessage;
import org.jed2k.protocol.ServerStatus;
import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.Serializable;
import org.jed2k.protocol.tag.Tag;

import static org.jed2k.protocol.tag.Tag.tag;

public class ServerConnection extends Connection {
    private static Logger log = Logger.getLogger(ServerConnection.class.getName());
    
    private ServerConnection(Session ses, 
            final InetSocketAddress address, 
            ByteBuffer incomingBuffer,
            ByteBuffer outgoingBuffer, 
            Session session) throws IOException {
        super(ses, address, incomingBuffer, outgoingBuffer, session);
    }    
    
    public static ServerConnection makeConnection(Session ses, final InetSocketAddress address) {
        try {
            ByteBuffer ibuff = ByteBuffer.allocate(4096);
            ByteBuffer obuff = ByteBuffer.allocate(4096);
            return  new ServerConnection(ses, address, ibuff, obuff, ses);
        } catch(ClosedChannelException e) {
            
        } catch(IOException e) {
            
        }
        
        return null;
    }
    
    @Override 
    public void onConnectable() {
        try {
            super.onConnectable();
            write(hello());
            return;
        } catch(JED2KException e) {
            log.warning(e.getMessage());
        }
        
        close();
    }
    
    private Serializable hello() throws JED2KException {
        LoginRequest login = new LoginRequest();
        int version = 0x3c;
        int capability = LoginRequest.CAPABLE_AUXPORT | LoginRequest.CAPABLE_NEWTAGS | LoginRequest.CAPABLE_UNICODE | LoginRequest.CAPABLE_LARGEFILES;
        int versionClient = (LoginRequest.JED2K_VERSION_MAJOR << 24) | (LoginRequest.JED2K_VERSION_MINOR << 17) | (LoginRequest.JED2K_VERSION_TINY << 10) | (1 << 7);

        login.hash = Hash.EMULE;
        login.point.ip = 0;
        login.point.port = (short)4661;
        
        login.properties.add(tag(Tag.CT_VERSION, null, version));
        login.properties.add(tag(Tag.CT_SERVER_FLAGS, null, capability));
        login.properties.add(tag(Tag.CT_NAME, null, "jed2k"));
        login.properties.add(tag(Tag.CT_EMULE_VERSION, null, versionClient));
        log.info(login.toString());
        return login;
    }

    @Override
    public void onServerIdChange(ServerIdChange value) throws JED2KException {
        log.info("server id changed: " + value);
        session.clientId = value.clientId;
        session.tcpFlags = value.tcpFlags;
        session.auxPort = value.auxPort;
    }

    @Override
    public void onServerInfo(ServerInfo value) throws JED2KException {
        log.info("server info: " + value);
    }

    @Override
    public void onServerList(ServerList value) throws JED2KException {
        log.info("server list: " + value);
    }

    @Override
    public void onServerMessage(ServerMessage value) throws JED2KException {
        log.info("server message: " + value);
    }

    @Override
    public void onServerStatus(ServerStatus value) throws JED2KException {
        log.info("server status: " + value);
    }

    @Override
    public void onSearchResult(SearchResult value) throws JED2KException {
        log.info("search result: " + value);
    }

    @Override
    public void onClientHello(ClientHello value) throws JED2KException {
        throw new JED2KException("Unsupported packet");        
    }

    @Override
    public void onClientHelloAnswer(ClientHelloAnswer value)
            throws JED2KException {
        throw new JED2KException("Unsupported packet");
    }

    @Override
    public void onClientExtHello(ClientExtHello value) throws JED2KException {
        throw new JED2KException("Unsupported packet");
    }

    @Override
    public void onClientExtHelloAnswer(ClientExtHelloAnswer value)
            throws JED2KException {
        throw new JED2KException("Unsupported packet");
    }

    @Override
    protected void onClose() {
        session.clientId = 0;
        session.tcpFlags = 0;
        session.auxPort = 0;
    }
}