package org.jed2k;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.logging.Logger;

import org.jed2k.alert.SearchResultAlert;
import org.jed2k.alert.ServerMessageAlert;
import org.jed2k.alert.ServerStatusAlert;
import org.jed2k.exception.BaseErrorCode;
import org.jed2k.exception.ErrorCode;
import org.jed2k.protocol.NetworkIdentifier;
import org.jed2k.protocol.client.*;
import org.jed2k.protocol.server.*;
import org.jed2k.protocol.Hash;
import org.jed2k.protocol.server.PacketCombiner;
import org.jed2k.protocol.server.search.SearchResult;
import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.Serializable;
import org.jed2k.protocol.tag.Tag;

import static org.jed2k.protocol.tag.Tag.tag;

public class ServerConnection extends Connection {
    private static Logger log = Logger.getLogger(ServerConnection.class.getName());
    private long lastPingTime = 0;

    private ServerConnection(ByteBuffer incomingBuffer,
            ByteBuffer outgoingBuffer,
            PacketCombiner packetCombiner,
            Session session) throws IOException {
        super(incomingBuffer, outgoingBuffer, packetCombiner, session);
    }

    public static ServerConnection makeConnection(Session ses) {
        try {
            ByteBuffer ibuff = ByteBuffer.allocate(18192);
            ByteBuffer obuff = ByteBuffer.allocate(2048);
            return  new ServerConnection(ibuff, obuff, new PacketCombiner(), ses);
        } catch(ClosedChannelException e) {

        } catch(IOException e) {

        }

        return null;
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
        login.point.ip = 0;
        login.point.port = (short)session.settings.listenPort;

        login.properties.add(tag(Tag.CT_VERSION, null, version));
        login.properties.add(tag(Tag.CT_SERVER_FLAGS, null, capability));
        login.properties.add(tag(Tag.CT_NAME, null, session.settings.clientName));
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
    }

    @Override
    public void write(Serializable packet) {
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
    public void onFoundFileSources(FoundFileSources value)
            throws JED2KException {
        Transfer transfer = session.transfers.get(value.hash);
        if (transfer != null) {
            for(final NetworkIdentifier endpoint: value.sources) {
                if (Utils.isLowId(endpoint.ip) && !Utils.isLowId(session.clientId) && !session.callbacks.contains(endpoint.ip)) {
                    sendCallbackRequest(endpoint.ip);
                    session.callbacks.add(endpoint.ip);
                } else {
                    transfer.addPeer(endpoint);
                }
            }
        }
    }

    @Override
    public void onCallbackRequestFailed(CallbackRequestFailed value) throws JED2KException {
        log.warning("callback request failed " + value.toString());
    }

    @Override
    public void onCallbackRequestIncoming(CallbackRequestIncoming value) throws JED2KException {
        log.info("incoming callback request " + value.point.toString());
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
    void secondTick(long currentSessionTime) {
        // ping server when feature enabled and timeout occured
        if (session.settings.serverPingTimeout > 0 &&
                currentSessionTime - lastPingTime > session.settings.serverPingTimeout) {
            log.info("Send ping message to server");
            write(new GetList());
        }
    }

    void sendFileSourcesRequest(final Hash h, final long size) {
        long hi = (size >>> 32) & 0xFFFFFFFF;
        long lo = size & 0xFFFFFFFF;
        write(new GetFileSources(h, (int)hi, (int)lo));
    }

    void sendCallbackRequest(final int clientId) {
        write(new CallbackRequest(clientId));
    }
}
