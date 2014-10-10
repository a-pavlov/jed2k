package org.jed2k;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

import org.jed2k.protocol.Hash;
import org.jed2k.protocol.LoginRequest;
import org.jed2k.protocol.NetworkBuffer;
import org.jed2k.protocol.ProtocolException;
import org.jed2k.protocol.Serializable;
import org.jed2k.protocol.tag.Tag;

import static org.jed2k.protocol.tag.Tag.tag;

public class ServerConnection {
    private final InetSocketAddress address;
    private SocketChannel socket;
    private ByteBuffer bufferIncoming;
    private ByteBuffer bufferOutgoing;
    private LinkedList<Serializable> outgoingOrder;
    
    public ServerConnection(final InetSocketAddress address) {
        
        this.address = address;
        bufferIncoming = ByteBuffer.allocate(1024);
        bufferOutgoing = ByteBuffer.allocate(1024);
    }
    
    public void connect() throws IOException, ProtocolException {
        socket = SocketChannel.open();
        socket.connect(address);
        LoginRequest login = new LoginRequest();
        int version = 0x3c;
        int capability = LoginRequest.CAPABLE_AUXPORT | LoginRequest.CAPABLE_NEWTAGS | LoginRequest.CAPABLE_UNICODE | LoginRequest.CAPABLE_LARGEFILES;
        int versionClient = (LoginRequest.JED2K_VERSION_MAJOR << 24) | (LoginRequest.JED2K_VERSION_MINOR << 17) | (LoginRequest.JED2K_VERSION_TINY << 10) | (1 << 7);

        login.hash = Hash.EMULE;
        login.point.ip = 0;
        login.point.port = 4661;

        login.properties.add(tag(Tag.CT_VERSION, null, version));
        login.properties.add(tag(Tag.CT_SERVER_FLAGS, null, capability));
        login.properties.add(tag(Tag.CT_NAME, null, "jed2k"));
        login.properties.add(tag(Tag.CT_EMULE_VERSION, null, versionClient));
        login.put(new NetworkBuffer(bufferOutgoing));
        while(bufferOutgoing.hasRemaining()) {
            socket.write(bufferOutgoing);
        }
    }
    
    public void close() {
        
    }
    
    public void write(Serializable packet) {
        //if (socket)
    }
}