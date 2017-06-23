package org.dkf.jed2k;

import org.dkf.jed2k.data.PeerRequest;
import org.dkf.jed2k.data.PieceBlock;
import org.dkf.jed2k.data.Region;
import org.dkf.jed2k.disk.AsyncHash;
import org.dkf.jed2k.disk.AsyncWrite;
import org.dkf.jed2k.exception.BaseErrorCode;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.BitField;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.Hash;
import org.dkf.jed2k.protocol.PacketCombiner;
import org.dkf.jed2k.protocol.client.*;
import org.dkf.jed2k.protocol.server.*;
import org.dkf.jed2k.protocol.server.search.SearchResult;
import org.dkf.jed2k.protocol.tag.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import static org.dkf.jed2k.protocol.tag.Tag.tag;

import org.dkf.jed2k.protocol.PacketCombiner;

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
|       HashSetRequest(file dataSize > 9M)     |
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

    public static final int MAX_OUTGOING_BUFFER_SIZE = 102*2 + 8; // look at PeerConnectionTest for details

    /**
     * peer connection speed over transfer's average speed
     */
    public enum PeerSpeed {
        SLOW,
        MEDIUM,
        FAST
    }

    private class PendingBlock {
        public PieceBlock block;
        long dataSize;
        long createTime;
        Region dataLeft;
        ByteBuffer buffer;

        /**
         * class for handle downloading block data
         * @param b requested piece block
         * @param totalSize dataSize of transfer
         */
        public PendingBlock(PieceBlock b, long totalSize) {
            assert totalSize > 0;
            block = b;
            this.dataSize = b.size(totalSize);
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
            return String.format("%s dataSize %d", block, dataSize);
        }
    }

    private static Logger log = LoggerFactory.getLogger(PeerConnection.class);

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
    private Endpoint endpoint;

    /**
     * for imcoming connections external originator is true
     */
    private boolean externalOriginator;

    PeerConnection(Endpoint point,
            ByteBuffer incomingBuffer,
            ByteBuffer outgoingBuffer,
            PacketCombiner packetCombiner,
            Session session,
            Transfer transfer, Peer peerInfo) throws IOException {
        super(incomingBuffer, outgoingBuffer, packetCombiner, session);
        this.transfer = transfer;
        endpoint = point;
        this.peerInfo = peerInfo;
        this.externalOriginator = false;
    }

    PeerConnection(ByteBuffer incomingBuffer,
            ByteBuffer outgoingBuffer,
            PacketCombiner packetCombiner,
            Session session,
            SocketChannel socket) throws IOException {
        super(incomingBuffer, outgoingBuffer, packetCombiner, session, socket);
        endpoint = new Endpoint();
        peerInfo = null;
        externalOriginator = true;
    }

    public static PeerConnection make(SocketChannel socket, Session session) throws JED2KException {
        try {
            ByteBuffer ibuff = ByteBuffer.allocate(8128);
            ByteBuffer obuff = ByteBuffer.allocate(MAX_OUTGOING_BUFFER_SIZE);
            return  new PeerConnection(ibuff, obuff, new org.dkf.jed2k.protocol.client.PacketCombiner(), session, socket);
        } catch(ClosedChannelException e) {
            throw new JED2KException(ErrorCode.CHANNEL_CLOSED);
        } catch(IOException e) {
            throw new JED2KException(ErrorCode.IO_EXCEPTION);
        }
    }

    public static PeerConnection make(Session ses, final Endpoint point, Transfer transfer, Peer peerInfo) throws JED2KException {
        try {
            ByteBuffer ibuff = ByteBuffer.allocate(8128);
            ByteBuffer obuff = ByteBuffer.allocate(MAX_OUTGOING_BUFFER_SIZE);
            return new PeerConnection(point, ibuff, obuff, new org.dkf.jed2k.protocol.client.PacketCombiner(), ses, transfer, peerInfo);
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
                log.error("error on read in transfer data mode {}", e.toString());
                close(e.getErrorCode());
            }
        } else {
            super.onReadable();
        }
    }

    @Override
    public Endpoint getEndpoint() {
        return endpoint;
    }

    public final boolean hasEndpoint() {
        return endpoint.defined();
    }

    public final void connect() throws JED2KException {
        assert endpoint != null;
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
            return  (aichVersion           << ((4*7)+1)) |
                    (unicodeSupport        << 4*7) |
                    (udpVer                << 4*6) |
                    (dataCompVer           << 4*5) |
                    (supportSecIdent       << 4*4) |
                    (sourceExchange1Ver    << 4*3) |
                    (extendedRequestsVer   << 4*2) |
                    (acceptCommentVer      << 4) |
                    (noViewSharedFiles     << 2) |
                    (multiPacket           << 1) |
                    supportsPreview;
        }

        public void assign(int value) {
            aichVersion          = (value >> (4*7+1)) & 0x07;
            unicodeSupport       = (value >> 4*7) & 0x01;
            udpVer               = (value >> 4*6) & 0x0f;
            dataCompVer          = (value >> 4*5) & 0x0f;
            supportSecIdent      = (value >> 4*4) & 0x0f;
            sourceExchange1Ver   = (value >> 4*3) & 0x0f;
            extendedRequestsVer  = (value >> 4*2) & 0x0f;
            acceptCommentVer     = (value >> 4) & 0x0f;
            noViewSharedFiles    = (value >> 2) & 0x01;
            multiPacket          = (value >> 1) & 0x01;
            supportsPreview      = value & 0x01;
        }
    }

    private class MiscOptions2 {
        private int value = 0;
        private static final int LARGE_FILE_OFFSET = 4;
        private static final int MULTIP_OFFSET = 5;
        private static final int SRC_EXT_OFFSET = 10;
        private static final int CAPTHA_OFFSET = 11;

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
        public Endpoint point = new Endpoint();
        public String modName;
        public int version = 0;
        public String modVersion;
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
        if (o instanceof PeerConnection && ((PeerConnection)o).hasEndpoint() && hasEndpoint()) {
            return endpoint.equals(((PeerConnection) o).endpoint);
        }
        return false;
    }

    public HelloAnswer prepareHello(final HelloAnswer hello) throws JED2KException {
        hello.hash.assign(session.getUserAgent());
        //Utils.fingerprint(hello.getHash, (byte)'M', (byte)'L');
        hello.point.setIP(session.getClientId());
        hello.point.setPort(session.getListenPort());

        hello.properties.add(Tag.tag(Tag.CT_NAME, null, session.getClientName()));
        hello.properties.add(Tag.tag(Tag.CT_MOD_VERSION, null, session.getModName()));
        hello.properties.add(Tag.tag(Tag.CT_VERSION, null, session.getAppVersion()));
        hello.properties.add(Tag.tag(Tag.CT_EMULE_UDPPORTS, null, 0));
        // do not send CT_EM_VERSION since it will activate secure identification we are not support

        MiscOptions mo = new MiscOptions();
        mo.unicodeSupport = 1;
        mo.dataCompVer = session.getCompressionVersion();  // support data compression
        mo.noViewSharedFiles = 1; // temp value
        mo.sourceExchange1Ver = 0; //SOURCE_EXCHG_LEVEL - important value

        MiscOptions2 mo2 = new MiscOptions2();
        mo2.setCaptcha();
        mo2.setLargeFiles();
        mo2.setSourceExt2();

        hello.properties.add(Tag.tag(Tag.CT_EMULE_VERSION, null, (int)Utils.makeFullED2KVersion(ClientSoftware.SO_AMULE.value,
                session.getModMajorVersion(),
                session.getModMinorVersion(),
                session.getModBuildVersion())));

        hello.properties.add(Tag.tag(Tag.CT_EMULE_MISCOPTIONS1, null, mo.intValue()));
        hello.properties.add(Tag.tag(Tag.CT_EMULE_MISCOPTIONS2, null, mo2.value));

        return hello;
    }

    private void assignRemotePeerInformation(HelloAnswer value) throws JED2KException {
        //remotePeerInfo.point
        Iterator<Tag> itr = value.properties.iterator();
        while(itr.hasNext()) {
            Tag tag = itr.next();
            switch(tag.getId()) {
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
                break;
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
        log.debug("onClientHello");
        assignRemotePeerInformation(value);
        endpoint.assign(value.point);
        Hash h = session.callbacks.get(value.point.getIP());
        Transfer t = null;

        log.debug("extracted hash {}", h!=null?h.toString():"null");

        if (h != null) {
            session.callbacks.remove(value.point.getIP());
            t = session.transfers.get(h);
            log.debug("extracted transfer {}", t!=null?t.toString():"null");
        }

        write(prepareHello(new HelloAnswer()));

        // attach incoming peer to transfer
        // will throw exception and close socket if transfer not in appropriate state
        if (t != null) {
            t.attachPeer(this);
            write(new FileRequest(t.getHash()));
        }
    }

    @Override
    public void onClientHelloAnswer(HelloAnswer value)
            throws JED2KException {
        assignRemotePeerInformation(value);
        if (transfer != null) {
            write(new FileRequest(transfer.getHash()));
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
            transfer.removePeerConnection(this);
            transfer = null;
        }

        session.closeConnection(this);
    }

    @Override
    void secondTick(long tickIntervalMS) {
        if (isDisconnecting()) return;

        // calculate statistics
        super.secondTick(tickIntervalMS);

        // check timeout on connection
        if (Time.currentTime() - lastReceive > session.settings.peerConnectionTimeout*1000) {
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
        if (transfer != null && value.hash.equals(transfer.getHash())) {
            write(new FileStatusRequest(transfer.getHash()));
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
                write(new HashSetRequest(transfer.getHash()));
            } else {
                // file contains only one getHash, so set getHash set with that one getHash
                final Hash h = value.hash;
                if (transfer.getHash().equals(value.hash)) {
                    transfer.setHashSet(h, new ArrayList<Hash>() {{
                        add(h);
                    }});
                    write(new StartUpload(transfer.getHash()));
                } else {
                    log.warn("getHash from response {} mismatch transfer's getHash {}"
                        , value.hash
                        , transfer.getHash());
                    close(ErrorCode.HASH_MISMATCH);
                }
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
            if (transfer.getHash().equals(value.getHash())
                    && transfer.getHash().equals(Hash.fromHashSet(value.getParts()))
                    && transfer.getPicker().getPieceCount() == value.getParts().size()) {
                transfer.setHashSet(value.getHash(), value.getParts());
                write(new StartUpload(transfer.getHash()));
            } else {
                log.warn("incorrect getHash set answer {} for transfer getHash {}"
                    , value.getHash()
                    , transfer.getHash());
                close(ErrorCode.WRONG_HASHSET);
            }
        } else {
            close(ErrorCode.NO_TRANSFER);
        }
    }

    @Override
    public void onClientNoFileStatus(NoFileStatus value)
            throws JED2KException {
        log.debug("{} << no file", getEndpoint());
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
        receiveCompressedData(value.beginOffset.longValue(), value.compressedLength.longValue(), value.bytesCount());
    }

    @Override
    public void onClientCompressedPart64(CompressedPart64 value) throws JED2KException {
        receiveCompressedData(value.beginOffset.longValue(), value.compressedLength.longValue(), value.bytesCount());
    }

    /**
     * prepare pending block according compressed block
     * prepare peer request
     * @param offset offset of data
     * @param compressedLength - length of compressed data in block
     * @param payloadSize - packet's bytes count
     * @throws JED2KException
     */
    void receiveCompressedData(long offset, long compressedLength, int payloadSize) throws JED2KException {
        assert header.isDefined();
        PieceBlock b = PieceBlock.make(offset);
        PendingBlock pb = getDownloading(b);

        // we received compressed block - it will whole block delimited or not delimited to few requests
        // in first time correct pending block range to adopt it to compressed block parameters
        // operation will execute one time per block
        if (pb != null && pb.buffer == null) {
            pb.dataSize = compressedLength; // actual block size
            pb.dataLeft.shrinkEnd(compressedLength);    // reduce block size here
            log.trace("block shrinked to {}", compressedLength);
        }

        // when pending block exists simply calculate offset as begin of remaining range
        long beginOffset = (pb != null)? pb.dataLeft.begin() : offset;
        long dataSize = header.sizePacket() - payloadSize;
        long endOffset = beginOffset + dataSize;
        log.trace("begin offset {} end offset {} data dataSize {}", beginOffset, endOffset, dataSize);

        // run calculated request
        receiveData(PeerRequest.mk_request(beginOffset, endOffset), true);
    }

    /**
     * start receiving of payload data
     * @param r received peer request will be set as current
     * @throws JED2KException
     */
    void receiveData(final PeerRequest r, final boolean compressed) throws JED2KException {
        //log.debug("{} receive data: {}", getEndpoint(), r);
        transferringData = true;
        recvReq = r;
        recvPos = 0;
        recvReqCompressed = compressed;
        PieceBlock b = PieceBlock.mkBlock(r);

        // search for correspond pending block in downloading queue
        PendingBlock pb = getDownloading(b);

        if (pb == null) {
            log.warn("{} have no correspond block for request {}, skip data", getEndpoint(), r);
            skipData();
            return;
        }

        // if pending block hasn't associated buffer - allocate it
        if (pb.buffer == null) pb.buffer = session.allocatePoolBuffer();

        /**
         * if buffer pool hasn't free blocks we will get null buffer
         * throw exception will lead of close connection with no memory error
         */
        if (pb.buffer == null) {
            log.warn("{} can not allocate buffer for block {} current request {}", getEndpoint(), b, r);
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
        log.debug("{} skipData {} bytes", getEndpoint(), (int)recvReq.length - recvPos);
        ByteBuffer buffer = session.allocateSkipDataBufer();
        buffer.clear();
        buffer.limit((int)recvReq.length - recvPos);

        try {
            int n = socket.read(buffer);
            if (n == -1) throw new JED2KException(ErrorCode.END_OF_STREAM);
            recvPos += n;
            log.trace("{} recvpos {} recvlen {}", getEndpoint(), recvPos, recvReq);
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

        assert recvReq != null;
        // search for correspond pending block
        PendingBlock pb = getDownloading(PieceBlock.mkBlock(recvReq));

        /**
         * check we have received block in our downloading queue
         * if we have no it skip data
         */
        if (pb == null) {
            log.warn("{} request {} has not correspond block in downloading queue, skip data", getEndpoint(), recvReq);
            skipData();
            return;
        }

        PieceBlock blockFinished = PieceBlock.mkBlock(recvReq);

        assert pb.buffer != null;

        /**
         * if block already was downloaded(depends on piece picker politics) - skip data
         */
        if (transfer.getPicker().isBlockDownloaded(blockFinished)) {
            log.warn("{} request {} references to downloaded block {}, remove pending block and skip data", getEndpoint(), recvReq, blockFinished);
            downloadQueue.remove(pb);
            skipData();
            return;
        }

        try {
            int n = socket.read(pb.buffer);
            if (n == -1) throw new JED2KException(ErrorCode.END_OF_STREAM);
            assert n != -1;

            recvPos += n;
            if (n != 0) lastReceive = Time.currentTime();
            statistics().receiveBytes(0, n);

            if (pb.buffer.remaining() == 0) {
                log.trace("{} received {} bytes for block {}, buffer is full, turn off transferring data"
                        , getEndpoint()
                        , pb.block
                        , recvPos);
                assert recvPos == recvReq.length;
                // turn off data transfer mode
                transferringData = false;

                if (completeBlock(pb)) {
                    log.debug("{} block {} completed, dataSize {}"
                            , getEndpoint()
                            , pb.block
                            , pb.dataSize);
                    // set position to zero - start reading from begin of buffer

                    assert pb.buffer.hasRemaining();
                    //assert(pb.buffer.remaining() == pb.dataSize);

                    boolean wasFinished = transfer.getPicker().isPieceFinished(recvReq.piece);
                    boolean wasDownloading = transfer.getPicker().markAsWriting(pb.block);
                    downloadQueue.remove(pb);

                    // was downloading means block was in downdloading or none state
                    // possibly block was already written in end game mode and/or finished
                    // in that case no need to re-write block to disk and request getHash
                    if (wasDownloading) {
                        // add write task to executor and add future to transfer
                        asyncWrite(pb.block, pb.buffer, transfer);

                        // run async getHash calculation
                        if (transfer.getPicker().isPieceFinished(recvReq.piece) && !wasFinished) {
                            asyncHash(pb.block.pieceIndex, transfer);
                        }
                    } else {
                        log.warn("{} block {} wasn't downloading, do not write"
                            , getEndpoint()
                            , pb.block);
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
        assert recvReq != null;
        assert recvReq.length == recvPos;
        assert pb.buffer != null;

        pb.dataLeft.sub(recvReq.range());

        // when we receive compressed part inflate it to temporary buffer and put it into receive buffer again
        if (pb.isCompleted()) {
            if (recvReqCompressed) {
                log.debug("compressed block completed {}", pb.dataSize);
                byte[] temp = session.allocateTemporaryInflateBuffer();
                byte[] zData = new byte[(int) pb.dataSize];
                pb.buffer.clear();
                pb.buffer.limit((int) pb.dataSize);
                pb.buffer.get(zData);

                Inflater decompresser = new Inflater();
                decompresser.setInput(zData, 0, zData.length);
                int resultLength = 0;
                try {
                    resultLength = decompresser.inflate(temp);
                    log.trace("Compressed data dataSize {} uncompressed data dataSize {}", zData.length, resultLength);
                } catch (DataFormatException e) {
                    throw new JED2KException(ErrorCode.INFLATE_ERROR);
                } finally {
                    decompresser.end();
                }

                assert resultLength != 0;

                // write inflated data into receive buffer and prepare buffer for reading
                pb.buffer.clear();
                pb.buffer.limit(resultLength);
                pb.buffer.put(temp, 0, resultLength);
                pb.buffer.flip();
            }
            else {
                // prepare buffer for reading
                pb.buffer.clear();
                pb.buffer.limit((int)pb.dataSize);
            }

            return true;
        }

        return false;
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

    /**
     * request new blocks from associated transfer's picker
     */
    void requestBlocks() {
        if (transfer == null || !transfer.hasPicker() || transferringData || !downloadQueue.isEmpty()) return;
        LinkedList<PieceBlock> blocks = new LinkedList<PieceBlock>();
        PiecePicker picker = transfer.getPicker();
        picker.pickPieces(blocks, Constants.REQUEST_QUEUE_SIZE, getPeer(), speed());
        RequestParts64 reqp = new RequestParts64(transfer.getHash());

        while(!blocks.isEmpty() && downloadQueue.size() < Constants.REQUEST_QUEUE_SIZE) {
            PieceBlock b = blocks.poll();
            downloadQueue.add(new PendingBlock(b, transfer.size()));
            assert !reqp.isFool();
            reqp.append(b.range(transfer.size()));
            // do not flush (write) structure to remote here like in libed2k since order always contain no more than 3 blocks
        }

        log.debug("request blocks completed, download queue dataSize {}", downloadQueue.size());
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
                picker.abortDownload(pb.block, getPeer());
                if (pb.buffer != null) {
                    pb.buffer.clear();
                    session.getBufferPool().deallocate(pb.buffer, Time.currentTime());
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
    void asyncWrite(final PieceBlock b, final ByteBuffer buffer, final Transfer t) {
        transfer.getPicker().markAsWriting(b);
        session.submitDiskTask(new AsyncWrite(b, buffer, t));
    }

    /**
     * submit hashing task to executor
     * @param pieceIndex index of piece which getHash should be calculated
     * @param t - transfer ?
     * @return future of hashing operation result
     */
    void asyncHash(int pieceIndex, final Transfer t) {
        session.submitDiskTask(new AsyncHash(t, pieceIndex));
    }

    /**
     *
     * @return information about remote peer
     */
    public final PeerInfo getInfo() {
        PeerInfo i = new PeerInfo();
        i.setDownloadPayload(statistics().totalPayloadDownload());
        i.setDownloadProtocol(statistics().totalProtocolDownload());
        i.setDownloadSpeed((int)statistics().downloadRate());
        i.setPayloadDownloadSpeed((int)statistics().downloadPayloadRate());
        i.setRemotePieces(remotePieces);
        i.setFailCount((getPeer()!=null)?getPeer().getFailCount():0);
        i.setModName(remotePeerInfo.modName);
        i.setVersion(remotePeerInfo.version);
        i.setModVersion(remotePeerInfo.modNumber);
        i.setEndpoint(getEndpoint());
        i.setStrModVersion(remotePeerInfo.modVersion);
        i.setSourceFlag((getPeer()!=null)?getPeer().getSourceFlag():0);
        return i;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getEndpoint()).append(" ").append(getPeer()!=null?getPeer().toString():"null");
        switch(speed()) {
            case SLOW:
                sb.append(" SLOW ");
                break;
            case MEDIUM:
                sb.append(" MEDIUM ");
                break;
            case FAST:
                sb.append(" FAST ");
                break;
            default:
                sb.append(" ? ");
                break;
        }
        return sb.toString();
    }
}
