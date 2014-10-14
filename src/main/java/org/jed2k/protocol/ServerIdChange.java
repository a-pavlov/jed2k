package org.jed2k.protocol;

import static org.jed2k.Utils.sizeof;

public class ServerIdChange implements Serializable {
    public int clientId = 0;
    public int tcpFlags = 0;
    public int auxPort  = 0;
    
    @Override
    public Buffer get(Buffer src) throws ProtocolException {
        clientId = src.getInt();
        tcpFlags = src.getInt();
        auxPort = src.getInt();
        return src;
    }

    @Override
    public Buffer put(Buffer dst) throws ProtocolException {
        return dst.put(clientId).put(tcpFlags).put(auxPort);
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
