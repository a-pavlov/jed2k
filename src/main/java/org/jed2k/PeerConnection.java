package org.jed2k;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.logging.Logger;

import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.BitField;
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
import org.jed2k.protocol.ClientPacketCombiner;
import org.jed2k.protocol.ClientRequestParts;
import org.jed2k.protocol.ClientRequestParts32;
import org.jed2k.protocol.ClientSendingPart32;
import org.jed2k.protocol.ClientSendingPart64;
import org.jed2k.protocol.FoundFileSources;
import org.jed2k.protocol.NetworkIdentifier;
import org.jed2k.protocol.PacketCombiner;
import org.jed2k.protocol.SearchResult;
import org.jed2k.protocol.ServerIdChange;
import org.jed2k.protocol.ServerInfo;
import org.jed2k.protocol.ServerList;
import org.jed2k.protocol.ServerMessage;
import org.jed2k.protocol.ServerStatus;
import org.jed2k.protocol.UInt32;
import org.jed2k.protocol.tag.Tag;

import static org.jed2k.protocol.tag.Tag.tag;

public class PeerConnection extends Connection {
    private static Logger log = Logger.getLogger(PeerConnection.class.getName());
    private boolean active = false;   // true when we connect to peer, false when incoming connection
    private RemotePeerInfo remotePeerInfo = new RemotePeerInfo();
    private Transfer transfer = null;
    private final NetworkIdentifier point = new NetworkIdentifier();
    private BitField bits = null;
    private PieceBlock[] interestedBlocks = { null, null, null };
    
    PeerConnection(NetworkIdentifier point,
            ByteBuffer incomingBuffer,
            ByteBuffer outgoingBuffer,
            PacketCombiner packetCombiner,
            Session session,
            Transfer transfer) throws IOException {
        super(incomingBuffer, outgoingBuffer, packetCombiner, session);
        this.transfer = transfer;
    }
    
    PeerConnection(ByteBuffer incomingBuffer,
            ByteBuffer outgoingBuffer,
            PacketCombiner packetCombiner,
            Session session, 
            SocketChannel socket) throws IOException {
        super(incomingBuffer, outgoingBuffer, packetCombiner, session, socket);
    }
    
    public static PeerConnection make(SocketChannel socket, Session session) {
        try {
            ByteBuffer ibuff = ByteBuffer.allocate(4096);
            ByteBuffer obuff = ByteBuffer.allocate(4096);
            return  new PeerConnection(ibuff, obuff, new ClientPacketCombiner(), session, socket);
        } catch(ClosedChannelException e) {
            
        } catch(IOException e) {
            
        }
        
        return null;
    }
    
    public static PeerConnection make(Session ses, final NetworkIdentifier point, Transfer transfer) {
        try {
            ByteBuffer ibuff = ByteBuffer.allocate(4096);
            ByteBuffer obuff = ByteBuffer.allocate(4096);
            return new PeerConnection(point, ibuff, obuff, new ClientPacketCombiner(), ses, transfer);
        } catch(ClosedChannelException e) {
            
        } catch(IOException e) {
            
        }
        
        return null;
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
    
    @Override
    public boolean equals(Object o) {
        /*if (o instanceof PeerConnection) {
            if (((PeerConnection)o).hasPort() && hasPort()) {
                return point.equals(((PeerConnection) o).point);
            } else {
                return point.ip == ((PeerConnection)o).point.ip; 
            }
        }
        */
        return false;
    }
    
    private ClientHelloAnswer prepareHello(final ClientHelloAnswer hello) throws JED2KException {
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
    
    private void assignRemotePeerInformation(ClientHelloAnswer value) throws JED2KException {
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
    public void onServerIdChange(ServerIdChange value) throws JED2KException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onServerInfo(ServerInfo value) throws JED2KException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onServerList(ServerList value) throws JED2KException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onServerMessage(ServerMessage value) throws JED2KException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onServerStatus(ServerStatus value) throws JED2KException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onSearchResult(SearchResult value) throws JED2KException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onClientHello(ClientHello value) throws JED2KException {
        // extract client information
        assignRemotePeerInformation(value);
        write(prepareHello(new ClientHelloAnswer()));
    }

    @Override
    public void onClientHelloAnswer(ClientHelloAnswer value)
            throws JED2KException {
        assignRemotePeerInformation(value);
        if (transfer != null) {
            write(new ClientFileRequest(transfer.fileHash()));
        }
    }

    @Override
    public void onClientExtHello(ClientExtHello value) throws JED2KException {
        // TODO Auto-generated method stub
        ClientExtHelloAnswer answer = new ClientExtHelloAnswer();
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
    public void onClientExtHelloAnswer(ClientExtHelloAnswer value)
            throws JED2KException {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void onConnect() throws JED2KException {
        write(prepareHello(new ClientHello()));
    }
    
    @Override
    protected void onDisconnect() {
        session.erasePeer(point);
    }

    @Override
    public void onClientFileRequest(ClientFileRequest value)
            throws JED2KException {
        close();        
    }

    @Override
    public void onClientFileAnswer(ClientFileAnswer value)
            throws JED2KException {
        if (transfer != null && value.hash.equals(transfer.fileHash())) {
            write(new ClientFileStatusRequest(transfer.fileHash()));
        } else {
            close();
        }
    }

    @Override
    public void onClientFileStatusRequest(ClientFileStatusRequest value)
            throws JED2KException {
        write(new ClientNoFileStatus());
    }

    @Override
    public void onClientFileStatusAnswer(ClientFileStatusAnswer value)
            throws JED2KException {
        bits = value.bitfield;
        if (transfer != null) {
            write(new ClientHashSetRequest(transfer.fileHash()));
        } else {
            close();
        }
    }

    @Override
    public void onClientHashSetRequest(ClientHashSetRequest value)
            throws JED2KException {
        close();
    }

    @Override
    public void onClientHashSetAnswer(ClientHashSetAnswer value)
            throws JED2KException {
        if (transfer != null && transfer.validateHashset(value.parts.collection)) {
            ClientRequestParts32 request = new ClientRequestParts32();
            int currentInterest = 0;
            for(int i = 0; i < ClientRequestParts.PARTS_COUNT; ++i) {
                PieceBlock interestedBlock = transfer.requestBlock();
                interestedBlocks[currentInterest++] = interestedBlock;
                
                if (interestedBlock != null) {
                    // generate packet
                    Pair<Long, Long> offset = Utils.range(interestedBlock, transfer.fileSize());
                    request.beginOffset.add(new UInt32(offset.left.intValue()));
                    request.endOffset.add(new UInt32(offset.right.intValue()));                    
                }
            }
            
            assert(request.beginOffset.size() == request.endOffset.size());
            
            if (!request.beginOffset.isEmpty()) {
                write(request);
            } else {
                close();
            }
        }
    }

    @Override
    public void onClientNoFileStatus(ClientNoFileStatus value)
            throws JED2KException {
        close();
    }

    @Override
    public void onClientOutOfParts(ClientOutOfParts value)
            throws JED2KException {
        close();        
    }

    @Override
    public void onFoundFileSources(FoundFileSources value)
            throws JED2KException {
        // TODO Auto-generated method stub
        
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
}

