package org.jed2k;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.security.acl.LastOwnerException;
import java.util.logging.Logger;

import org.jed2k.protocol.ClientExtHello;
import org.jed2k.protocol.ClientExtHelloAnswer;
import org.jed2k.protocol.ClientFileAnswer;
import org.jed2k.protocol.ClientFileRequest;
import org.jed2k.protocol.ClientFileStatusAnswer;
import org.jed2k.protocol.ClientFileStatusRequest;
import org.jed2k.protocol.ClientHashSetAnswer;
import org.jed2k.protocol.ClientHashSetRequest;
import org.jed2k.protocol.ClientHello;
import org.jed2k.protocol.ClientHelloAnswer;
import org.jed2k.protocol.ClientNoFileStatus;
import org.jed2k.protocol.ClientOutOfParts;
import org.jed2k.protocol.ClientSendingPart32;
import org.jed2k.protocol.ClientSendingPart64;
import org.jed2k.protocol.server.FoundFileSources;
import org.jed2k.protocol.Hash;
import org.jed2k.protocol.server.LoginRequest;
import org.jed2k.protocol.server.search.SearchResult;
import org.jed2k.protocol.server.IdChange;
import org.jed2k.protocol.server.ServerInfo;
import org.jed2k.protocol.server.ServerList;
import org.jed2k.protocol.server.Message;
import org.jed2k.protocol.server.PacketCombiner;
import org.jed2k.protocol.server.Status;
import org.jed2k.protocol.server.GetList;
import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.Serializable;
import org.jed2k.protocol.tag.Tag;

import static org.jed2k.protocol.tag.Tag.tag;

public class ServerConnection extends Connection {
    private static Logger log = Logger.getLogger(ServerConnection.class.getName());
    
    private ServerConnection(ByteBuffer incomingBuffer,
            ByteBuffer outgoingBuffer, 
            PacketCombiner packetCombiner,
            Session session) throws IOException {
        super(incomingBuffer, outgoingBuffer, packetCombiner, session);
    }    
    
    public static ServerConnection makeConnection(Session ses) {
        try {
            ByteBuffer ibuff = ByteBuffer.allocate(4096);
            ByteBuffer obuff = ByteBuffer.allocate(4096);
            return  new ServerConnection(ibuff, obuff, new PacketCombiner(), ses);
        } catch(ClosedChannelException e) {
            
        } catch(IOException e) {
            
        }
        
        return null;
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
    public void onServerIdChange(IdChange value) throws JED2KException {
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
    public void onServerMessage(Message value) throws JED2KException {
        log.info("server message: " + value);
    }

    @Override
    public void onServerStatus(Status value) throws JED2KException {
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
    protected void onConnect() throws JED2KException {
        write(hello());
    }

    @Override
    protected void onDisconnect() {
        session.clientId = 0;
        session.tcpFlags = 0;
        session.auxPort = 0;
    }

    @Override
    public void onClientFileRequest(ClientFileRequest value)
            throws JED2KException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onClientFileAnswer(ClientFileAnswer value)
            throws JED2KException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onClientFileStatusRequest(ClientFileStatusRequest value)
            throws JED2KException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onClientFileStatusAnswer(ClientFileStatusAnswer value)
            throws JED2KException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onClientHashSetRequest(ClientHashSetRequest value)
            throws JED2KException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onClientHashSetAnswer(ClientHashSetAnswer value)
            throws JED2KException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onClientNoFileStatus(ClientNoFileStatus value)
            throws JED2KException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onClientOutOfParts(ClientOutOfParts value)
            throws JED2KException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onFoundFileSources(FoundFileSources value)
            throws JED2KException {
        Transfer transfer = session.transfers.get(value.hash);
        if (transfer != null) {
            transfer.setupSources(value.sources.collection);
        }
    }

    @Override
    public void onClientSendingPart32(ClientSendingPart32 value)
            throws JED2KException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onClientSendingPart64(ClientSendingPart64 value)
            throws JED2KException {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void secondTick(long time_interval_ms) {        
        // ping server when feature enabled and timeout occured
        if (session.settings.serverPingTimeout > 0 && 
                Time.currentTime() - lastTick > session.settings.serverPingTimeout) {
            log.info("Send ping message to server");
            write(new GetList());
        }
    }
}
