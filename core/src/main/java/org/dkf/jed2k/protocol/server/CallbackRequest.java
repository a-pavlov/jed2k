package org.dkf.jed2k.protocol.server;

import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Serializable;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import static org.dkf.jed2k.Utils.sizeof;

public class CallbackRequest implements Serializable {
    public int clientId = 0;

    public CallbackRequest(int c) {
        clientId = c;
    }

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        try {
            clientId = src.getInt();
        } catch(BufferUnderflowException e) {
            throw new JED2KException(ErrorCode.BUFFER_UNDERFLOW_EXCEPTION);
        } catch(Exception e) {
            throw new JED2KException(ErrorCode.BUFFER_GET_EXCEPTION);
        }
        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return dst.putInt(clientId);
    }

    @Override
    public int bytesCount() {
        return sizeof(clientId);
    }

}
