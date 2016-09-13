package org.dkf.jed2k;

import org.dkf.jed2k.alert.*;
import org.dkf.jed2k.exception.BaseErrorCode;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Hash;
import org.dkf.jed2k.protocol.NetworkIdentifier;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.client.*;
import org.dkf.jed2k.protocol.server.*;
import org.dkf.jed2k.protocol.server.PacketCombiner;
import org.dkf.jed2k.protocol.server.search.SearchRequest;
import org.dkf.jed2k.protocol.server.search.SearchResult;
import org.dkf.jed2k.protocol.tag.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;

import static org.dkf.jed2k.protocol.tag.Tag.tag;

public class ServerConnection extends Connection {
    private static Logger log = LoggerFactory.getLogger(ServerConnection.class);
    private long lastPingTime = 0;
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
        log.debug("login ", login);
        return login;
    }

    @Override
    public void onServerIdChange(IdChange value) throws JED2KException {
        log.info("server id changed to {}", value);
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
        log.info("search result: " + value);
        session.pushAlert(new SearchResultAlert(value));
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
            for(final NetworkIdentifier endpoint: value.sources) {
                if (Utils.isLowId(endpoint.getIP()) && !Utils.isLowId(session.clientId) && !session.callbacks.containsKey(endpoint.getIP())) {
                    sendCallbackRequest(endpoint.getIP());
                    session.callbacks.put(endpoint.getIP(), value.hash);
                } else {
                    log.debug("to hash {} added endpoint {}", value.hash, endpoint);
                    try {
                        transfer.addPeer(endpoint);
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
        // ping server when feature enabled and timeout occured
        if (session.settings.serverPingTimeout > 0 &&
                currentSessionTime - lastPingTime > session.settings.serverPingTimeout*1000) {
            log.debug("Send ping message to server");
            write(new GetList());
        }
    }

    @Override
    NetworkIdentifier getEndpoint() {
        // actually we have server ip/port but currently I no need them for debug purposes
        return new NetworkIdentifier();
    }

    void sendFileSourcesRequest(final Hash h, final long size) {
        long hi = (size >>> 32) & 0xFFFFFFFF;
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
