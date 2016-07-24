package org.jed2k;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.logging.Logger;

import org.jed2k.alert.SearchResultAlert;
import org.jed2k.alert.ServerMessageAlert;
import org.jed2k.alert.ServerStatusAlert;
import org.jed2k.protocol.client.*;
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
            ByteBuffer ibuff = ByteBuffer.allocate(8192);
            ByteBuffer obuff = ByteBuffer.allocate(8192);
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
        session.pushAlert(new ServerMessageAlert(value.toString()));
    }

    @Override
    public void onServerStatus(Status value) throws JED2KException {
        log.info("server status: " + value);
        session.pushAlert(new ServerStatusAlert(value.filesCount, value.usersCount));
    }

    @Override
    public void onSearchResult(SearchResult value) throws JED2KException {
        log.info("search result: " + value);
        session.pushAlert(new SearchResultAlert(value));
    }

    @Override
    public void onClientHello(Hello value) throws JED2KException {
        throw new JED2KException("Unsupported packet");        
    }

    @Override
    public void onClientHelloAnswer(HelloAnswer value)
            throws JED2KException {
        throw new JED2KException("Unsupported packet");
    }

    @Override
    public void onClientExtHello(ExtHello value) throws JED2KException {
        throw new JED2KException("Unsupported packet");
    }

    @Override
    public void onClientExtHelloAnswer(ExtHelloAnswer value)
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
    public void onClientFileRequest(FileRequest value)
            throws JED2KException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onClientFileAnswer(FileAnswer value)
            throws JED2KException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onClientFileStatusRequest(FileStatusRequest value)
            throws JED2KException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onClientFileStatusAnswer(FileStatusAnswer value)
            throws JED2KException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onClientHashSetRequest(HashSetRequest value)
            throws JED2KException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onClientHashSetAnswer(HashSetAnswer value)
            throws JED2KException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onClientNoFileStatus(NoFileStatus value)
            throws JED2KException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onClientOutOfParts(OutOfParts value)
            throws JED2KException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onFoundFileSources(FoundFileSources value)
            throws JED2KException {
        Transfer transfer = session.transfers.get(value.hash);
        if (transfer != null) {
            //transfer.setupSources(value.sources.collection);
        }
    }

    @Override
    public void onClientSendingPart32(SendingPart32 value)
            throws JED2KException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onClientSendingPart64(SendingPart64 value)
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
