package org.dkf.jed2k;

import org.dkf.jed2k.alert.*;
import org.dkf.jed2k.exception.BaseErrorCode;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.Hash;
import org.dkf.jed2k.protocol.SearchEntry;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.client.*;
import org.dkf.jed2k.protocol.server.*;
import org.dkf.jed2k.protocol.server.PacketCombiner;
import org.dkf.jed2k.protocol.server.search.SearchMore;
import org.dkf.jed2k.protocol.server.search.SearchRequest;
import org.dkf.jed2k.protocol.server.search.SearchResult;
import org.dkf.jed2k.protocol.tag.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.LinkedList;
import java.util.List;

import static org.dkf.jed2k.protocol.tag.Tag.tag;

public class ServerConnection extends Connection {
    private static Logger log = LoggerFactory.getLogger(ServerConnection.class);
    private long lastPingTime = Time.currentTime();
    private boolean handshakeCompleted = false;

    /**
     * special identifier for server connection
     * will be added to each server alert
     */
    private final String identifier;

    private ServerConnection(
            final String id,
            ByteBuffer incomingBuffer,
            ByteBuffer outgoingBuffer,
            PacketCombiner packetCombiner,
            Session session) throws IOException {
        super(incomingBuffer, outgoingBuffer, packetCombiner, session);
        identifier = id;
    }

    public static ServerConnection makeConnection(final String id, Session ses) throws JED2KException {
        try {
            ByteBuffer ibuff = ByteBuffer.allocate(1024);
            ByteBuffer obuff = ByteBuffer.allocate(1048);
            return  new ServerConnection(id, ibuff, obuff, new PacketCombiner(), ses);
        } catch(ClosedChannelException e) {
            throw new JED2KException(ErrorCode.CHANNEL_CLOSED);
        } catch(IOException e) {
            throw new JED2KException(ErrorCode.IO_EXCEPTION);
        }
    }

    public void search(final SearchRequest sr) {
        write(sr);
    }
    public void searchMore() {
        write(new SearchMore());
    }

    private Serializable hello() throws JED2KException {
        LoginRequest login = new LoginRequest();
        int version = 0x3c;
        int capability = LoginRequest.CAPABLE_AUXPORT
                | LoginRequest.CAPABLE_NEWTAGS
                | LoginRequest.CAPABLE_UNICODE
                | LoginRequest.CAPABLE_LARGEFILES
                | LoginRequest.CAPABLE_ZLIB;

        int versionClient = (LoginRequest.JED2K_VERSION_MAJOR << 24) | (LoginRequest.JED2K_VERSION_MINOR << 17) | (LoginRequest.JED2K_VERSION_TINY << 10) | (1 << 7);

        login.hash = session.settings.userAgent;
        login.point.setIP(0);
        login.point.setPort((short)session.settings.listenPort);

        login.properties.add(tag(Tag.CT_VERSION, null, version));
        login.properties.add(tag(Tag.CT_SERVER_FLAGS, null, capability));
        login.properties.add(tag(Tag.CT_NAME, null, session.settings.clientName));
        login.properties.add(tag(Tag.CT_EMULE_VERSION, null, versionClient));
        log.debug("login {}\nVERSION {} SERVER_FLAGS {} EMULE_VERSION {}"
                , login
                , version
                , capability
                , versionClient);

        return login;
    }

    @Override
    public void onServerIdChange(IdChange value) throws JED2KException {
        log.debug("server id changed to {}", value);
        session.clientId = value.clientId;
        session.tcpFlags = value.tcpFlags;
        session.auxPort = value.auxPort;
        handshakeCompleted = true;
        session.pushAlert(new ServerIdAlert(identifier, value.clientId));
    }

    @Override
    public void onServerInfo(ServerInfo value) throws JED2KException {
        log.debug("server info {}", value);
        session.pushAlert(new ServerInfoAlert(identifier, value));
    }

    @Override
    public void onServerList(ServerList value) throws JED2KException {
        log.debug("server list {}", value);
    }

    @Override
    public void onServerMessage(Message value) throws JED2KException {
        log.debug("server message {}", value);
        session.pushAlert(new ServerMessageAlert(identifier, value.asString()));
    }

    @Override
    public void onServerStatus(Status value) throws JED2KException {
        log.debug("server status {}", value);
        session.pushAlert(new ServerStatusAlert(identifier, value.filesCount, value.usersCount));
    }

    @Override
    public void onSearchResult(SearchResult value) throws JED2KException {
        log.trace("search result: " + value);
        // transform server's search result to common
        List<SearchEntry> entries = new LinkedList<>();
        if (value.getResults().getList() != null) {
            for (final SharedFileEntry entry : value.getResults().getList()) {
                entries.add(entry);
            }
        }

        session.pushAlert(new SearchResultAlert(entries, value.hasMoreResults()));
    }

    @Override
    public void onClientHello(Hello value) throws JED2KException {
        throw new JED2KException(ErrorCode.SERVER_CONN_UNSUPPORTED_PACKET);
    }

    @Override
    public void onClientHelloAnswer(HelloAnswer value)
            throws JED2KException {
        throw new JED2KException(ErrorCode.SERVER_CONN_UNSUPPORTED_PACKET);
    }

    @Override
    public void onClientExtHello(ExtHello value) throws JED2KException {
        throw new JED2KException(ErrorCode.SERVER_CONN_UNSUPPORTED_PACKET);
    }

    @Override
    public void onClientExtHelloAnswer(ExtHelloAnswer value)
            throws JED2KException {
        throw new JED2KException(ErrorCode.SERVER_CONN_UNSUPPORTED_PACKET);
    }

    @Override
    protected void onConnect() throws JED2KException {
        write(hello());
    }

    @Override
    protected void onDisconnect(BaseErrorCode ec) {
        // remove server connection from session
        session.clientId = 0;
        session.tcpFlags = 0;
        session.auxPort = 0;
        session.serverConection = null;
        handshakeCompleted = false;
        session.pushAlert(new ServerConectionClosed(identifier, ec));
    }

    @Override
    protected void write(Serializable packet) {
        super.write(packet);
        lastPingTime = session.getCurrentTime();
    }

    @Override
    public void onClientFileRequest(FileRequest value)
            throws JED2KException {
        throw new JED2KException(ErrorCode.SERVER_CONN_UNSUPPORTED_PACKET);
    }

    @Override
    public void onClientFileAnswer(FileAnswer value)
            throws JED2KException {
        throw new JED2KException(ErrorCode.SERVER_CONN_UNSUPPORTED_PACKET);
    }

    @Override
    public void onClientFileStatusRequest(FileStatusRequest value)
            throws JED2KException {
        throw new JED2KException(ErrorCode.SERVER_CONN_UNSUPPORTED_PACKET);
    }

    @Override
    public void onClientFileStatusAnswer(FileStatusAnswer value)
            throws JED2KException {
        throw new JED2KException(ErrorCode.SERVER_CONN_UNSUPPORTED_PACKET);
    }

    @Override
    public void onClientHashSetRequest(HashSetRequest value)
            throws JED2KException {
        throw new JED2KException(ErrorCode.SERVER_CONN_UNSUPPORTED_PACKET);
    }

    @Override
    public void onClientHashSetAnswer(HashSetAnswer value)
            throws JED2KException {
        throw new JED2KException(ErrorCode.SERVER_CONN_UNSUPPORTED_PACKET);
    }

    @Override
    public void onClientNoFileStatus(NoFileStatus value)
            throws JED2KException {
        throw new JED2KException(ErrorCode.SERVER_CONN_UNSUPPORTED_PACKET);
    }

    @Override
    public void onClientOutOfParts(OutOfParts value)
            throws JED2KException {
        throw new JED2KException(ErrorCode.SERVER_CONN_UNSUPPORTED_PACKET);
    }

    @Override
    public void onAcceptUpload(AcceptUpload value) throws JED2KException {
        throw new JED2KException(ErrorCode.SERVER_CONN_UNSUPPORTED_PACKET);
    }

    @Override
    public void onQueueRanking(QueueRanking value) throws JED2KException {
        throw new JED2KException(ErrorCode.SERVER_CONN_UNSUPPORTED_PACKET);
    }

    @Override
    public void onFoundFileSources(FoundFileSources value)
            throws JED2KException {
        Transfer transfer = session.transfers.get(value.hash);
        if (transfer != null) {
            log.debug("onFoundSources {}", value.sources.size());
            log.debug("session: {}", Utils.isLowId(session.clientId)?"LOW":"HI");
            for(final Endpoint endpoint: value.sources) {
                if (Utils.isLowId(endpoint.getIP())) {
                    log.debug("Low ID endpoint detected {}", endpoint);
                    if (!Utils.isLowId(session.clientId) && !session.callbacks.containsKey(endpoint.getIP())) {
                        log.debug("send callback request to {} for hash {}", endpoint, value.hash);
                        sendCallbackRequest(endpoint.getIP());
                        session.callbacks.put(endpoint.getIP(), value.hash);
                    }
                } else {
                    log.debug("to getHash {} added endpoint {}", value.hash, endpoint);
                    try {
                        transfer.addPeer(endpoint, PeerInfo.SERVER);
                    } catch(JED2KException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onCallbackRequestFailed(CallbackRequestFailed value) throws JED2KException {
        log.debug("callback request failed {}" + value);
    }

    @Override
    public void onCallbackRequestIncoming(CallbackRequestIncoming value) throws JED2KException {
        log.debug("incoming callback request ", value.point);
    }

    @Override
    public void onClientSendingPart32(SendingPart32 value)
            throws JED2KException {
        throw new JED2KException(ErrorCode.SERVER_CONN_UNSUPPORTED_PACKET);
    }

    @Override
    public void onClientSendingPart64(SendingPart64 value)
            throws JED2KException {
        throw new JED2KException(ErrorCode.SERVER_CONN_UNSUPPORTED_PACKET);
    }

    @Override
    public void onClientCompressedPart32(CompressedPart32 value) throws JED2KException {
        throw new JED2KException(ErrorCode.SERVER_CONN_UNSUPPORTED_PACKET);
    }

    @Override
    public void onClientCompressedPart64(CompressedPart64 value) throws JED2KException {
        throw new JED2KException(ErrorCode.SERVER_CONN_UNSUPPORTED_PACKET);
    }

    @Override
    void secondTick(long currentSessionTime) {
        super.secondTick(currentSessionTime);

        if (session.settings.serverPingTimeout > 0) {
            // server ping enabled + server connection timeout verification
            long currentTime = Time.currentTime();

            if (getMillisecondSinceLastReceive() > session.settings.serverPingTimeout*1.5*1000) {
                log.info("timeout on server response, close connection");
                close(ErrorCode.CONNECTION_TIMEOUT);
            } else if (currentTime - lastPingTime > session.settings.serverPingTimeout*1000) {
                log.debug("Send ping message to server");
                lastPingTime = currentTime;
                write(new GetList());
            }
        }
    }

    @Override
    Endpoint getEndpoint() {
        // actually we have server ip/port but currently I no need them for debug purposes
        return new Endpoint();
    }

    void sendFileSourcesRequest(final Hash h, final long size) {
        long hi = size >>> 32;
        long lo = size & 0xFFFFFFFF;
        write(new GetFileSources(h, (int)hi, (int)lo));
    }

    void sendCallbackRequest(final int clientId) {
        write(new CallbackRequest(clientId));
    }

    public final String getIdentifier() {
        return identifier;
    }

    public boolean isHandshakeCompleted() { return handshakeCompleted; }
}
