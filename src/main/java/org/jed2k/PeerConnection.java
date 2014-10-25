package org.jed2k;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;

import org.jed2k.exception.JED2KException;
import org.jed2k.hash.MD4;
import org.jed2k.protocol.ClientExtHello;
import org.jed2k.protocol.ClientExtHelloAnswer;
import org.jed2k.protocol.ClientHello;
import org.jed2k.protocol.ClientHelloAnswer;
import org.jed2k.protocol.NetworkIdentifier;
import org.jed2k.protocol.PacketCombiner;
import org.jed2k.protocol.SearchResult;
import org.jed2k.protocol.ServerIdChange;
import org.jed2k.protocol.ServerInfo;
import org.jed2k.protocol.ServerList;
import org.jed2k.protocol.ServerMessage;
import org.jed2k.protocol.ServerPacketCombiner;
import org.jed2k.protocol.ServerStatus;
import org.jed2k.protocol.tag.Tag;

import com.sun.corba.se.spi.ior.MakeImmutable;

public class PeerConnection extends Connection {
    NetworkIdentifier point;
    private boolean active = false;   // true when we connect to peer, false when incoming connection
    
    PeerConnection(InetSocketAddress address, 
            ByteBuffer incomingBuffer,
            ByteBuffer outgoingBuffer,
            PacketCombiner packetCombiner,
            Session session) throws IOException {
        super(address, incomingBuffer, outgoingBuffer, packetCombiner, session);
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
            return  new PeerConnection(ibuff, obuff, new ServerPacketCombiner(), session, socket);
        } catch(ClosedChannelException e) {
            
        } catch(IOException e) {
            
        }
        
        return null;
    }
    
    private class MiscOptions {
        int aichVersion = 0;
        int unicodeSupport = 0;
        int udpVer = 0;
        int dataCompVer = 0;
        int supportSecIdent = 0;
        int sourceExchange1Ver = 0;
        int extendedRequestsVer = 0;
        int acceptCommentVer = 0;
        int noViewSharedFiles = 0;
        int multiPacket = 0;
        int supportsPreview = 0;
        
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
    }
    
    private class MiscOptions2 {
        public int value = 0;
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
        hello.point.ip = session.clientId;
        hello.point.port = session.settings.listenPort;
        
        hello.properties.add(Tag.tag(Tag.CT_NAME, null, session.settings.clientName));
        hello.properties.add(Tag.tag(Tag.CT_MOD_VERSION, null, session.settings.modName));
        hello.properties.add(Tag.tag(Tag.CT_VERSION, null, session.settings.version));
        hello.properties.add(Tag.tag(Tag.CT_EMULE_UDPPORTS, null, 0));
        
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
        write(prepareHello(new ClientHelloAnswer()));
    }

    @Override
    public void onClientHelloAnswer(ClientHelloAnswer value)
            throws JED2KException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onClientExtHello(ClientExtHello value) throws JED2KException {
        // TODO Auto-generated method stub
        
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
        
    }
}

