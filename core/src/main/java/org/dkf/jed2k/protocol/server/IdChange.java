package org.dkf.jed2k.protocol.server;

import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Dispatchable;
import org.dkf.jed2k.protocol.Dispatcher;
import org.dkf.jed2k.protocol.SoftSerializable;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import static org.dkf.jed2k.Utils.sizeof;

public class IdChange extends SoftSerializable implements Dispatchable {
    public int clientId = 0;
    public int tcpFlags = 0;
    public int auxPort  = 0;

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        try {
            clientId = src.getInt();
            tcpFlags = src.getInt();
            auxPort = src.getInt();
            return src;
        } catch(BufferUnderflowException e) {
            throw new JED2KException(ErrorCode.BUFFER_UNDERFLOW_EXCEPTION);
        } catch(Exception e) {
            throw new JED2KException(ErrorCode.BUFFER_GET_EXCEPTION);
        }
    }

    @Override
    public ByteBuffer get(ByteBuffer src, int limit) throws JED2KException {
        try {
            clientId = src.getInt();
            limit -= sizeof(clientId);

            if (limit >= sizeof(tcpFlags)) {
                tcpFlags = src.getInt();
                limit -= sizeof(tcpFlags);
            }

            if (limit >= sizeof(auxPort)) {
                auxPort = src.getInt();
            }

            return src;
        } catch(BufferUnderflowException e) {
            throw new JED2KException(ErrorCode.BUFFER_UNDERFLOW_EXCEPTION);
        } catch(Exception e) {
            throw new JED2KException(ErrorCode.BUFFER_GET_EXCEPTION);
        }
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
