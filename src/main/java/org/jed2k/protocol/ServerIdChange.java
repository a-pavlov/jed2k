package org.jed2k.protocol;

import static org.jed2k.Utils.sizeof;

import java.nio.ByteBuffer;

public class ServerIdChange extends SoftSerializable {
    public int clientId = 0;
    public int tcpFlags = 0;
    public int auxPort  = 0;
    
    @Override
    public ByteBuffer get(ByteBuffer src) throws ProtocolException {
        clientId = src.getInt();
        tcpFlags = src.getInt();
        auxPort = src.getInt();
        return src;
    }
    
    @Override
    public ByteBuffer get(ByteBuffer src, int limit) {
        clientId = src.getInt();
        limit -= sizeof(clientId);
        
        if (limit >= sizeof(tcpFlags)) {
            tcpFlags = src.getInt();
            limit -= sizeof(tcpFlags);
        }
        
        if (limit >= sizeof(auxPort)) {
            auxPort = src.getInt();
            limit -= sizeof(auxPort);
        }
        
        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws ProtocolException {
        return dst.putInt(clientId).putInt(tcpFlags).putInt(auxPort);
    }

    @Override
    public int size() {
        return sizeof(clientId) + sizeof(tcpFlags) + sizeof(auxPort);        
    }
    
    @Override
    public String toString() {
        return "Id: " + clientId + " tcpf: " + tcpFlags + " auxp: " + auxPort;
    }
}
