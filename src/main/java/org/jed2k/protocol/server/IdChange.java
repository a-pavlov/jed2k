package org.jed2k.protocol.server;

import org.jed2k.protocol.Dispatchable;
import org.jed2k.protocol.Dispatcher;
import org.jed2k.protocol.SoftSerializable;
import org.jed2k.exception.JED2KException;
import java.nio.ByteBuffer;
import static org.jed2k.Utils.sizeof;

public class IdChange extends SoftSerializable implements Dispatchable {
    public int clientId = 0;
    public int tcpFlags = 0;
    public int auxPort  = 0;
    
    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
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
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return dst.putInt(clientId).putInt(tcpFlags).putInt(auxPort);
    }

    @Override
    public int bytesCount() {
        return sizeof(clientId) + sizeof(tcpFlags) + sizeof(auxPort);        
    }
    
    @Override
    public String toString() {
        return "Id: " + clientId + " tcpf: " + tcpFlags + " auxp: " + auxPort;
    }

    @Override
    public void dispatch(Dispatcher dispatcher) throws JED2KException {
        dispatcher.onServerIdChange(this);
    }
}
