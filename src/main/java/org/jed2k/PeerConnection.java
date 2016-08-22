package org.jed2k;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.Future;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.jed2k.data.*;
import org.jed2k.data.PeerRequest;
import org.jed2k.exception.BaseErrorCode;
import org.jed2k.exception.JED2KException;
import org.jed2k.exception.ErrorCode;
import org.jed2k.protocol.BitField;
import org.jed2k.protocol.Hash;
import org.jed2k.protocol.client.*;
import org.jed2k.protocol.server.*;
import org.jed2k.protocol.NetworkIdentifier;
import org.jed2k.protocol.PacketCombiner;
import org.jed2k.protocol.server.search.SearchResult;
import org.jed2k.protocol.tag.Tag;

import static org.jed2k.protocol.tag.Tag.tag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
Usual packets order in case of we establish connection to remote peer
                                         remote peer
+                                          +
|       Hello                              |
+------------------------------------------>
|       HelloAnswer                        |
<------------------------------------------+
|       ExtHello(opt)                      |
+------------------------------------------>
|       ExtHelloAnswer(opt)                |
<------------------------------------------+
|       FileRequest                        |
+------------------------------------------>
|       FileAnswer                         |
<------------------------------------------+
|       FileStatusRequest                  |
+------------------------------------------>
|       FileStatusAnswer/NoFileStatus      |
<------------------------------------------+
|       HashSetRequest(file size > 9M)     |
+------------------------------------------>
|       HashSetAnswer                      |
<------------------------------------------+
|       RequestParts32/64                  |
+------------------------------------------>
|       SendingParts32/64 or OutOfParts    |
<------------------------------------------+
|                                          |
+                                          +

For incoming connection we have different order in first two packets
                                         remote peer
+                                          +
|       Hello                              |
<------------------------------------------+
|       HelloAnswer                        |
+------------------------------------------>
|       ExtHello(opt)                      |
<------------------------------------------+
|       ExtHelloAnswer(opt)                |
+------------------------------------------>
|       FileRequest for LowID source       |
+------------------------------------------>

*/
public class PeerConnection extends Connection {

    /**
     * peer connection speed over transfer's average speed
     */
    enum PeerSpeed {
        SLOW,
        MEDIUM,
        FAST
    }

    private class PendingBlock {
        public PieceBlock block;
        long size;
        long createTime;
        Region dataLeft;
        ByteBuffer buffer;

        /**
         * class for handle downloading block data
         * @param b requested piece block
         * @param totalSize size of transfer
         */
        public PendingBlock(PieceBlock b, long totalSize) {
            assert(totalSize > 0);
            block = b;
            this.size = b.size(totalSize);
            buffer = null;
            createTime = Time.currentTime();
            dataLeft = new Region(b.range(totalSize));
        }

        public boolean isCompleted() {
            return dataLeft.empty();
        }

        int compareTo(PieceBlock b) {
            return block.compareTo(b);
        }

        @Override
        public String toString() {
            return String.format("%s size %d", block, size);
        }
    }

    private static Logger log = LoggerFactory.getLogger(PeerConnection.class);

    //TODO - check it, possibly remove and use getPeer value instead
    private boolean active = false;   // true when we connect to peer, false when incoming connection
    private RemotePeerInfo remotePeerInfo = new RemotePeerInfo();

    /**
     * peer connection's transfer
     * null for incoming connections before attach to transfer
     */
    private Transfer transfer = null;

    /**
     * pieces available in remote endpoint
     * null before we get packet with file status answer
     */
    private BitField remotePieces = null;

    /**
     * calculated peer speed
     */
    private PeerSpeed speed = PeerSpeed.SLOW;

    /**
     * record in policy for this peer connection
     */
    private Peer peerInfo;

    private boolean failed = false;

    /**
     * channel transferring data
     * peer request header has been read and we awaiting or already reading payload data
     */
    private boolean transferringData = false;

    /**
     * current peer request from remote peer
     * next will be payload data
     */
    private PeerRequest recvReq = null;

    /**
     * special flag if current peer request is compressed
     */
    private boolean recvReqCompressed = false;

    /**
     * offset in current peer request
     */
    private int recvPos = 0;

    private LinkedList<PendingBlock> downloadQueue = new LinkedList<PendingBlock>();

    /**
     * network endpoint for outgoing connections
     */
    private NetworkIdentifier endpoint;

    PeerConnection(NetworkIdentifier point,
            ByteBuffer incomingBuffer,
            ByteBuffer outgoingBuffer,
            PacketCombiner packetCombiner,
            Session session,
            Transfer transfer, Peer peerInfo) throws IOException {
        super(incomingBuffer, outgoingBuffer, packetCombiner, session);
        this.transfer = transfer;
        endpoint = point;
        this.peerInfo = peerInfo;
    }

    PeerConnection(ByteBuffer incomingBuffer,
            ByteBuffer outgoingBuffer,
            PacketCombiner packetCombiner,
            Session session,
            SocketChannel socket) throws IOException {
        super(incomingBuffer, outgoingBuffer, packetCombiner, session, socket);
        endpoint = new NetworkIdentifier();
        peerInfo = null;
    }

    public static PeerConnection make(SocketChannel socket, Session session) throws JED2KException {
        try {
            ByteBuffer ibuff = ByteBuffer.allocate(8128);
            ByteBuffer obuff = ByteBuffer.allocate(8128);
            return  new PeerConnection(ibuff, obuff, new org.jed2k.protocol.client.PacketCombiner(), session, socket);
        } catch(ClosedChannelException e) {
            throw new JED2KException(ErrorCode.CHANNEL_CLOSED);
        } catch(IOException e) {
            throw new JED2KException(ErrorCode.IO_EXCEPTION);
        }
    }

    public static PeerConnection make(Session ses, final NetworkIdentifier point, Transfer transfer, Peer peerInfo) throws JED2KException {
        try {
            ByteBuffer ibuff = ByteBuffer.allocate(8128);
            ByteBuffer obuff = ByteBuffer.allocate(8128);
            return new PeerConnection(point, ibuff, obuff, new org.jed2k.protocol.client.PacketCombiner(), ses, transfer, peerInfo);
        } catch(ClosedChannelException e) {
            throw new JED2KException(ErrorCode.CHANNEL_CLOSED);
        } catch(IOException e) {
            throw new JED2KException(ErrorCode.IO_EXCEPTION);
        }
    }

    @Override
    public void onReadable() {
        if (transferringData) {
            try {
                onReceiveData();
            } catch(JED2KException e) {
                log.error("error on read in transfer data mode {}", e);
                close(e.getErrorCode());
            }
        } else {
            super.onReadable();
        }
    }

    @Override
    public NetworkIdentifier getEndpoint() {
        return endpoint;
    }

    public final boolean hasEndpoint() {
        return endpoint.defined();
    }

    public final void connect() throws JED2KException {
        assert(endpoint != null);
        super.connect(endpoint.toInetSocketAddress());
    }

    private class MiscOptions {
        public int aichVersion = 0;
        public int unicodeSupport = 0;
        public int udpVer = 0;
        public int dataCompVer = 0;
        public int supportSecIdent = 0;
        public int sourceExchange1Ver = 0;
        public int extendedRequestsVer = 0;
        public int acceptCommentVer = 0;
        public int noViewSharedFiles = 0;
        public int multiPacket = 0;
        public int supportsPreview = 0;

        public int intValue() {
            return  ((aichVersion           << ((4*7)+1)) |
                    (unicodeSupport        << 4*7) |
                    (udpVer                << 4*6) |
                    (dataCompVer           << 4*5) |
                    (supportSecIdent       << 4*4) |
                    (sourceExchange1Ver    << 4*3) |
                    (extendedRequestsVer   << 4*2) |
                    (acceptCommentVer      << 4*1) |
                    (noViewSharedFiles     << 1*2) |
                    (multiPacket           << 1*1) |
                    (supportsPreview       << 1*0));
        }

        public void assign(int value) {
            aichVersion          = (value >> (4*7+1)) & 0x07;
            unicodeSupport       = (value >> 4*7) & 0x01;
            udpVer               = (value >> 4*6) & 0x0f;
            dataCompVer          = (value >> 4*5) & 0x0f;
            supportSecIdent      = (value >> 4*4) & 0x0f;
            sourceExchange1Ver   = (value >> 4*3) & 0x0f;
            extendedRequestsVer  = (value >> 4*2) & 0x0f;
            acceptCommentVer     = (value >> 4*1) & 0x0f;
            noViewSharedFiles    = (value >> 1*2) & 0x01;
            multiPacket          = (value >> 1*1) & 0x01;
            supportsPreview      = (value >> 1*0) & 0x01;
        }
    }

    private class MiscOptions2 {
        private int value = 0;
        private final int LARGE_FILE_OFFSET = 4;
        private final int MULTIP_OFFSET = 5;
        private final int SRC_EXT_OFFSET = 10;
        private final int CAPTHA_OFFSET = 11;

        public boolean supportCaptcha() {
            return ((value >> CAPTHA_OFFSET) & 0x01) == 1;
        }

        public boolean supportSourceExt2() {
            return ((value >> SRC_EXT_OFFSET) & 0x01) == 1;
        }

        public boolean supportExtMultipacket() {
            return ((value >> MULTIP_OFFSET) & 0x01) == 1;
        }

        public boolean supportLargeFiles() {
            return ((value >> LARGE_FILE_OFFSET) & 0x01) == 0;
        }

        public void setCaptcha() {
            value |= 1 << CAPTHA_OFFSET;
        }

        public void setSourceExt2() {
            value |= 1 << SRC_EXT_OFFSET;
        }

        public void setExtMultipacket() {
            value |= 1 << MULTIP_OFFSET;
        }

        public void setLargeFiles() {
            value |= 1 << LARGE_FILE_OFFSET;
        }

        public void assign(int value) {
            this.value = value;
        }
    }

    public class RemotePeerInfo {
        public NetworkIdentifier point = new NetworkIdentifier();
        public String modName = new String();
        public int version = 0;
        public String modVersion = new String();
        public int modNumber = 0;
        public MiscOptions misc1 = new MiscOptions();
        public MiscOptions2 misc2 = new MiscOptions2();
    }

    /**
     * temporary count all peer connections without endpoints(incoming) are different
     * @param o - peer connection
     * @return true if both peer connections have equals endpoint and false elsewhere
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof PeerConnection) {
            if (((PeerConnection)o).hasEndpoint() && hasEndpoint()) {
                return endpoint.equals(((PeerConnection) o).endpoint);
            }
        }

        return false;
    }

    private HelloAnswer prepareHello(final HelloAnswer hello) throws JED2KException {
        hello.hash.assign(session.settings.userAgent);
        //Utils.fingerprint(hello.hash, (byte)'M', (byte)'L');
        hello.point.ip = session.clientId;
        hello.point.port = session.settings.listenPort;

        hello.properties.add(Tag.tag(Tag.CT_NAME, null, session.settings.clientName));
        hello.properties.add(Tag.tag(Tag.CT_MOD_VERSION, null, session.settings.modName));
        hello.properties.add(Tag.tag(Tag.CT_VERSION, null, session.settings.version));
        hello.properties.add(Tag.tag(Tag.CT_EMULE_UDPPORTS, null, 0));
        // do not send CT_EM_VERSION since it will activate secure identification we are not support

        MiscOptions mo = new MiscOptions();
        mo.unicodeSupport = 1;
        mo.dataCompVer = 0;  // support data compression
        mo.noViewSharedFiles = 1; // temp value
        mo.sourceExchange1Ver = 0; //SOURCE_EXCHG_LEVEL - important value

        MiscOptions2 mo2 = new MiscOptions2();
        mo2.setCaptcha();
        mo2.setLargeFiles();
        mo2.setSourceExt2();

        hello.properties.add(Tag.tag(Tag.CT_EMULE_VERSION, null, Utils.makeFullED2KVersion(ClientSoftware.SO_AMULE.value,
                session.settings.modMajor,
                session.settings.modMinor,
                session.settings.modBuild)));

        hello.properties.add(Tag.tag(Tag.CT_EMULE_MISCOPTIONS1, null, mo.intValue()));
        hello.properties.add(Tag.tag(Tag.CT_EMULE_MISCOPTIONS2, null, mo2.value));

        return hello;
    }

    private void assignRemotePeerInformation(HelloAnswer value) throws JED2KException {
        //remotePeerInfo.point
        Iterator<Tag> itr = value.properties.iterator();
        while(itr.hasNext()) {
            Tag tag = itr.next();
            switch(tag.id()) {
            case Tag.CT_NAME:
                remotePeerInfo.modName = tag.stringValue();
                break;
            case Tag.CT_VERSION:
                remotePeerInfo.version = tag.intValue();
                break;
            case Tag.CT_MOD_VERSION:
                if (tag.isStringTag()) remotePeerInfo.modVersion = tag.stringValue();
                else if (tag.isNumberTag()) remotePeerInfo.modNumber = tag.intValue();
                break;
            case Tag.CT_PORT:
                //m_options.m_nPort = p->asInt();
            case Tag.CT_EMULE_UDPPORTS:
                //m_options.m_nUDPPort = p->asInt() & 0xFFFF;
                //dwEmuleTags |= 1;
            case Tag.CT_EMULE_BUDDYIP:
                // 32 BUDDY IP
                //m_options.m_buddy_point.m_nIP = p->asInt();
            case Tag.CT_EMULE_BUDDYUDP:
                //m_options.m_buddy_point.m_nPort = p->asInt();
                //break;
                break;
            case Tag.CT_EMULE_MISCOPTIONS1:
                remotePeerInfo.misc1.assign(tag.intValue());
                break;
            case Tag.CT_EMULE_MISCOPTIONS2:
                remotePeerInfo.misc2.assign(tag.intValue());
                break;

            // Special tag for Compat. Clients Misc options.
            case Tag.CT_EMULECOMPAT_OPTIONS:
                //  1 Operative System Info
                //  1 Value-based-type int tags (experimental!)
                //m_options.m_bValueBasedTypeTags   = (p->asInt() >> 1*1) & 0x01;
                //m_options.m_bOsInfoSupport        = (p->asInt() >> 1*0) & 0x01;
            case Tag.CT_EMULE_VERSION:
                //  8 Compatible Client ID
                //  7 Mjr Version (Doesn't really matter..)
                //  7 Min Version (Only need 0-99)
                //  3 Upd Version (Only need 0-5)
                //  7 Bld Version (Only need 0-99)
                //m_options.m_nCompatibleClient = (p->asInt() >> 24);
                //m_options.m_nClientVersion = p->asInt() & 0x00ffffff;
                break;
            default:
                break;
            }
        }
    }

    @Override
    public void onServerIdChange(IdChange value) throws JED2KException {
        throw new JED2KException(ErrorCode.PEER_CONN_UNSUPPORTED_PACKET);
    }

    @Override
    public void onServerInfo(ServerInfo value) throws JED2KException {
        throw new JED2KException(ErrorCode.PEER_CONN_UNSUPPORTED_PACKET);
    }

    @Override
    public void onServerList(ServerList value) throws JED2KException {
        throw new JED2KException(ErrorCode.PEER_CONN_UNSUPPORTED_PACKET);
    }

    @Override
    public void onServerMessage(Message value) throws JED2KException {
        throw new JED2KException(ErrorCode.PEER_CONN_UNSUPPORTED_PACKET);
    }

    @Override
    public void onServerStatus(Status value) throws JED2KException {
        throw new JED2KException(ErrorCode.PEER_CONN_UNSUPPORTED_PACKET);
    }

    @Override
    public void onSearchResult(SearchResult value) throws JED2KException {
        throw new JED2KException(ErrorCode.PEER_CONN_UNSUPPORTED_PACKET);
    }

    @Override
    public void onCallbackRequestFailed(CallbackRequestFailed value) throws JED2KException {
        throw new JED2KException(ErrorCode.PEER_CONN_UNSUPPORTED_PACKET);
    }

    @Override
    public void onCallbackRequestIncoming(CallbackRequestIncoming value) throws JED2KException {
        throw new JED2KException(ErrorCode.PEER_CONN_UNSUPPORTED_PACKET);
    }

    @Override
    public void onFoundFileSources(FoundFileSources value)
            throws JED2KException {
        throw new JED2KException(ErrorCode.PEER_CONN_UNSUPPORTED_PACKET);
    }

    @Override
    public void onClientHello(Hello value) throws JED2KException {
        // extract client information
        assignRemotePeerInformation(value);
        endpoint.assign(value.point);
        Hash h = session.callbacks.get(value.point);
        Transfer t = null;

        if (h != null) {
            session.callbacks.remove(value.point);
            t = session.transfers.get(h);
        }

        // attach incoming peer to transfer
        // will throw exception and close socket if transfer not in appropriate state
        if (t != null) {
            t.attachPeer(this);
        }

        write(prepareHello(new HelloAnswer()));
    }

    @Override
    public void onClientHelloAnswer(HelloAnswer value)
            throws JED2KException {
        assignRemotePeerInformation(value);
        if (transfer != null) {
            write(new FileRequest(transfer.hash()));
        }
    }

    @Override
    public void onClientExtHello(ExtHello value) throws JED2KException {
        ExtHelloAnswer answer = new ExtHelloAnswer();
        answer.version.assign(0x10); // temp value
        answer.properties.add(tag(Tag.ET_COMPRESSION, null, 0));
        answer.properties.add(tag(Tag.ET_UDPPORT, null, 0));
        answer.properties.add(tag(Tag.ET_UDPVER, null, 0));
        answer.properties.add(tag(Tag.ET_SOURCEEXCHANGE, null, 0));
        answer.properties.add(tag(Tag.ET_COMMENTS, null, 0));
        answer.properties.add(tag(Tag.ET_EXTENDEDREQUEST, null, 0));
        answer.properties.add(tag(Tag.ET_COMPATIBLECLIENT, null, ClientSoftware.SO_AMULE.value)); // TODO - check it
        answer.properties.add(tag(Tag.ET_FEATURES, null, 0));
        answer.properties.add(tag(Tag.ET_MOD_VERSION, null, session.settings.version));
        write(answer);
    }

    @Override
    public void onClientExtHelloAnswer(ExtHelloAnswer value)
            throws JED2KException {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onConnect() throws JED2KException {
        write(prepareHello(new Hello()));
    }

    @Override
    protected void onDisconnect(BaseErrorCode ec) {
        if (ec != ErrorCode.NO_ERROR) failed = true;

        if (transfer != null) {
            transfer.addStats(statistics());
            abortAllRequests();
            transfer = null;
        }

        session.closeConnection(this);
    }

    @Override
    void secondTick(long currentSessionTime) {
        if (isDisconnecting()) return;

        // calculate statistics
        super.secondTick(currentSessionTime);

        // check timeout on connection
        if (currentSessionTime - lastReceive > session.settings.peerConnectionTimeout*1000) {
            close(ErrorCode.CONNECTION_TIMEOUT);
        }
    }

    @Override
    public void onClientFileRequest(FileRequest value)
            throws JED2KException {
        close(ErrorCode.NO_ERROR);
    }

    @Override
    public void onClientFileAnswer(FileAnswer value)
            throws JED2KException {
        log.debug("{} << file answer", endpoint);
        if (transfer != null && value.hash.equals(transfer.hash())) {
            write(new FileStatusRequest(transfer.hash()));
        } else {
            close(ErrorCode.NO_TRANSFER);
        }
    }

    @Override
    public void onClientFileStatusRequest(FileStatusRequest value)
            throws JED2KException {
        write(new NoFileStatus());
    }

    @Override
    public void onClientFileStatusAnswer(FileStatusAnswer value)
            throws JED2KException {
        log.debug("{} << file status answer", endpoint);
        remotePieces = value.bitfield;
        if (transfer != null) {
            if (transfer.size() >= Constants.PIECE_SIZE) {
                write(new HashSetRequest(transfer.hash()));
            } else {
                // file contains only one hash, so set hash set with that one hash
                final Hash h = value.hash;
                transfer.setHashSet(h, new ArrayList<Hash>() {{ add(h); }});
                write(new StartUpload(transfer.hash()));
            }
        } else {
            close(ErrorCode.NO_TRANSFER);
        }
    }

    @Override
    public void onClientHashSetRequest(HashSetRequest value)
            throws JED2KException {
        close(ErrorCode.NO_ERROR);
    }

    @Override
    public void onClientHashSetAnswer(HashSetAnswer value)
            throws JED2KException {
        log.debug("{} << hashset answer", endpoint);
        if (transfer != null) {
            transfer.setHashSet(value.hash, value.parts);
            write(new StartUpload(transfer.hash()));
        } else {
            close(ErrorCode.NO_TRANSFER);
        }
    }

    @Override
    public void onClientNoFileStatus(NoFileStatus value)
            throws JED2KException {
        close(ErrorCode.FILE_NOT_FOUND);
    }

    @Override
    public void onClientOutOfParts(OutOfParts value)
            throws JED2KException {
        close(ErrorCode.OUT_OF_PARTS);
    }

    @Override
    public void onAcceptUpload(AcceptUpload value) throws JED2KException {
        log.debug("{} << accept upload", endpoint);
        requestBlocks();
    }

    @Override
    public void onQueueRanking(QueueRanking value) throws JED2KException {
        log.debug("{} << queue ranking {} ", endpoint, value.rank);
        close(ErrorCode.QUEUE_RANKING);
    }

    @Override
    public void onClientSendingPart32(SendingPart32 value)
            throws JED2KException {
        receiveData(PeerRequest.mk_request(value.beginOffset.longValue(), value.endOffset.longValue()), false);
    }

    @Override
    public void onClientSendingPart64(SendingPart64 value)
            throws JED2KException {
        receiveData(PeerRequest.mk_request(value.beginOffset.longValue(), value.endOffset.longValue()), false);
    }

    @Override
    public void onClientCompressedPart32(CompressedPart32 value) throws JED2KException {
        receiveData(PeerRequest.mk_request(value.beginOffset.longValue(), value.beginOffset.longValue() + value.compressedLength.longValue()), true);
    }

    @Override
    public void onClientCompressedPart64(CompressedPart64 value) throws JED2KException {
        receiveData(PeerRequest.mk_request(value.beginOffset.longValue(), value.beginOffset.longValue() + value.compressedLength.longValue()), true);
    }

    /**
     * start receiving of payload data
     * @param r received peer request will be set as current
     * @throws JED2KException
     */
    void receiveData(final PeerRequest r, final boolean compressed) throws JED2KException {
        log.debug("receive data: {}", r);
        transferringData = true;
        recvReq = r;
        recvPos = 0;
        recvReqCompressed = compressed;
        PieceBlock b = PieceBlock.mk_block(r);

        // search for correspond pending block in downloading queue
        PendingBlock pb = getDownloading(b);

        if (pb == null) {
            log.warn("have no correspond block for request {}, skip data", r);
            skipData();
            return;
        }

        // if pending block hasn't associated buffer - allocate it
        if (pb.buffer == null) pb.buffer = allocateBuffer();

        /**
         * if buffer pool hasn't free blocks we will get null buffer
         * throw exception will lead of close connection with no memory error
         */
        if (pb.buffer == null) {
            log.warn("can not allocate buffer for block {} current request {}", b, r);
            throw new JED2KException(ErrorCode.NO_MEMORY);
        }

        // prepare buffer for reading data into proper place
        pb.buffer.position((int)r.inBlockOffset());
        pb.buffer.limit((int)(r.inBlockOffset() + r.length));
        onReceiveData();
    }

    /**
     * receive data into skip data buffer and ignore it
     * when we read unexpected request completely send new request to remote peer
     * and continue reading cycle skippind unexpected data and awaiting requested data
     * hope it is not a problem when peer receives new request in the middle of sending data
     */
    void skipData() throws JED2KException {
        log.debug("skipData {} bytes", (int)recvReq.length - recvPos);
        ByteBuffer buffer = session.allocateSkipDataBufer();
        buffer.limit((int)recvReq.length - recvPos);

        try {
            int n = socket.read(buffer);
            if (n == -1) throw new JED2KException(ErrorCode.END_OF_STREAM);
            recvPos += n;
            statistics().receiveBytes(0, n);
            if (n != 0) lastReceive = Time.currentTime();

            if (recvPos < recvReq.length) {
                // request more data from socket
                doRead();
            } else {
                // check block completed
                // we completed receive data of current peer request actually it is not mean block completed
                // but turn off data transferring mode and try to request more blocks
                transferringData = false;
                requestBlocks();
            }
        }
        catch(IOException e) {
            throw new JED2KException(ErrorCode.IO_EXCEPTION);
        }
    }

    /**
     * call this method each time when OP_READ event on socket occurred and peer connection
     * in transferring data mode. It will continue reading data of current peer request
     * @throws JED2KException
     */
    void onReceiveData() throws JED2KException {

        assert(recvReq != null);
        // search for correspond pending block
        PendingBlock pb = getDownloading(PieceBlock.mk_block(recvReq));

        /**
         * check we have received block in our downloading queue
         * if we have no it skip data
         */
        if (pb == null) {
            log.warn("request {} has not correspond block in downloading queue, skip data", recvReq);
            skipData();
            return;
        }

        PieceBlock blockFinished = PieceBlock.mk_block(recvReq);

        assert(pb.buffer != null);

        /**
         * if block already was downloaded(depends on piece picker politics) - skip data
         */
        if (transfer.getPicker().isBlockDownloaded(blockFinished)) {
            log.warn("request {} references to downloaded block {}, skip data", recvReq, blockFinished);
            skipData();
            return;
        }

        try {
            int n = socket.read(pb.buffer);
            if (n == -1) throw new JED2KException(ErrorCode.END_OF_STREAM);
            assert(n != -1);

            recvPos += n;
            if (n != 0) lastReceive = Time.currentTime();
            statistics().receiveBytes(0, n);

            if (pb.buffer.remaining() == 0) {
                log.debug("received {} bytes, buffer is full, turn off transferring data", recvPos);
                assert(recvPos == recvReq.length);
                // turn off data transfer mode
                transferringData = false;

                if (completeBlock(pb)) {
                    log.debug("block {} completed, size {}", pb.block, pb.size);
                    // set position to zero - start reading from begin of buffer
                    pb.buffer.clear();
                    // set buffer limit to whole block range
                    pb.buffer.limit((int)pb.size);

                    assert(pb.buffer.hasRemaining());
                    assert(pb.buffer.remaining() == pb.size);

                    boolean wasFinished = transfer.getPicker().isPieceFinished(recvReq.piece);
                    transfer.getPicker().markAsWriting(pb.block);
                    downloadQueue.remove(pb);
                    // add write task to executor and add future to transfer
                    transfer.aioFutures.addLast(asyncWrite(pb.block, pb.buffer, transfer));

                    // run async hash calculation
                    if (transfer.getPicker().isPieceFinished(recvReq.piece) && !wasFinished) {
                        transfer.aioFutures.addLast(asyncHash(pb.block.pieceIndex, transfer));
                    }

                    // write block to disk here
                    // remove pending block from downloading queue
                    // check piece finished and run hashing
                    // check download queue empty and request new blocks
                    requestBlocks();
                    return;
                }
            }
            // we are interested in event when socket has bytes for read again
            doRead();
        } catch(IOException e) {
            throw new JED2KException(ErrorCode.IO_EXCEPTION);
        }
    }

    /**
     * if request is compressed - inflate it and create real(uncompressed range)
     * put inflated data back to the block's buffer
     * update range in pending block and check block is completed
     * @param pb pending block from downloading queue
     * @return true if block completely downloaded
     * @throws JED2KException
     */
    boolean completeBlock(final PendingBlock pb) throws JED2KException {
        assert(recvReq != null);
        assert(recvReq.length == recvPos);
        assert(pb.buffer != null);

        // when we receive compressed part inflate it to temporary buffer and put it into receive buffer again
        if (recvReqCompressed) {
            byte[] temp = session.allocateTemporaryInflateBuffer();
            byte[] zData = new byte[(int)recvReq.length];
            Inflater decompresser = new Inflater();
            decompresser.setInput(zData, 0, zData.length);
            int resultLength = 0;
            try {
                resultLength = decompresser.inflate(temp);
                log.trace("Compressed data size {} uncompressed data size {}", zData.length, resultLength);
            } catch(DataFormatException e) {
                throw new JED2KException(ErrorCode.INFLATE_ERROR);
            } finally {
                decompresser.end();
            }

            assert(resultLength != 0);

            // write inflated data into receive buffer
            pb.buffer.clear();
            pb.buffer.limit(resultLength);
            pb.buffer.put(temp, 0, resultLength);

            // generate inflated range using original offset and length of uncompressed data
            pb.dataLeft.sub(new Range(recvReq.range().left, recvReq.range().left + resultLength));
        } else {
            // simply subtract received range from pending range
            pb.dataLeft.sub(recvReq.range());
        }

        return pb.isCompleted();
    }

    /**
     *
     * @return byte buffer with fixed size 180K from global session pool
     */
    ByteBuffer allocateBuffer() {
        return session.bufferPool.allocate();
    }

    public PeerSpeed speed() {
        if (transfer != null) {
            long downloadRate = statistics().downloadPayloadRate();
            long transferDownloadRate = transfer.statistics().downloadPayloadRate();

            if (downloadRate > 512 && downloadRate > transferDownloadRate / 16)
                speed = PeerSpeed.FAST;
            else if (downloadRate > 4096 && downloadRate > transferDownloadRate / 64)
                speed = PeerSpeed.MEDIUM;
            else if (downloadRate < transferDownloadRate / 15 && speed == PeerSpeed.FAST)
                speed = PeerSpeed.MEDIUM;
            else
                speed = PeerSpeed.SLOW;
        }

        return this.speed;
    }

    public Peer getPeer() {
        return peerInfo;
    }

    void setPeer(Peer peer) {
        peerInfo = peer;
    }

    public boolean isFailed() {
        return failed;
    }

    public boolean isRequesting(PieceBlock b) {
        for(PendingBlock pb: downloadQueue) {
            if (pb.block.compareTo(b) == 0) return true;
        }

        return false;
    }

    PendingBlock getDownloading(final PieceBlock b) {
        for(final PendingBlock pb: downloadQueue) {
            if (pb.compareTo(b) == 0) return pb;
        }

        return null;
    }

    void requestBlocks() {
        if (transfer == null || transferringData || !downloadQueue.isEmpty()) return;
        LinkedList<PieceBlock> blocks = new LinkedList<PieceBlock>();
        PiecePicker picker = transfer.getPicker();
        picker.pickPieces(blocks, Constants.REQUEST_QUEUE_SIZE);
        RequestParts64 reqp = new RequestParts64(transfer.hash());

        while(!blocks.isEmpty() && downloadQueue.size() < Constants.REQUEST_QUEUE_SIZE) {
            PieceBlock b = blocks.poll();
            // mark block as downloading
            transfer.getPicker().markAsDownloading(b); // ?
            downloadQueue.add(new PendingBlock(b, transfer.size()));
            assert(!reqp.isFool());
            reqp.append(b.range(transfer.size()));
            // do not flush (write) structure to remote here like in libed2k since order always contain no more than 3 blocks
        }

        log.debug("request blocks completed, download queue size {}", downloadQueue.size());
        if (!reqp.isEmpty()) {
            write(reqp);
        }
        else {
            close(ErrorCode.NO_ERROR);
        }
    }

    void abortAllRequests() {
        if (transfer != null && transfer.hasPicker()) {
            PiecePicker picker = transfer.getPicker();
            while(!downloadQueue.isEmpty()) {
                PendingBlock pb = downloadQueue.poll();
                picker.abortDownload(pb.block);
                if (pb.buffer != null) {
                    pb.buffer.clear();
                    session.bufferPool.deallocate(pb.buffer, Time.currentTime());
                }
            }
        }
        else {
            downloadQueue.clear();
        }
    }

    BitField getRemotePieces() {
        return remotePieces;
    }

    /**
     * submit task to executor service to async write block to disk
     * and return future
     * @param b completed block
     * @param buffer data buffer
     * @param t actual transfer
     * @return future of result for async operation
     */
    Future<AsyncOperationResult> asyncWrite(final PieceBlock b, final ByteBuffer buffer, final Transfer t) {
        transfer.getPicker().markAsWriting(b);
        return session.diskIOService.submit(new AsyncWrite(b, buffer, t));
    }

    /**
     * submit hashing task to executor
     * @param pieceIndex index of piece which hash should be calculated
     * @param t - transfer ?
     * @return future of hasshing operation result
     */
    Future<AsyncOperationResult> asyncHash(int pieceIndex, final Transfer t) {
        AsyncHash ah = new AsyncHash(transfer, pieceIndex);
        return session.diskIOService.submit(new AsyncHash(t, pieceIndex));
    }
}
