package org.dkf.jed2k.protocol.server;

import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Dispatchable;
import org.dkf.jed2k.protocol.Dispatcher;
import org.dkf.jed2k.protocol.Serializable;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import static org.dkf.jed2k.Utils.sizeof;

public class Status implements Serializable, Dispatchable {
    public int usersCount = 0;
    public int filesCount = 0;

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        try {
            usersCount = src.getInt();
            filesCount = src.getInt();
            return src;
        } catch(BufferUnderflowException e) {
            throw new JED2KException(ErrorCode.BUFFER_UNDERFLOW_EXCEPTION);
        } catch(Exception e) {
            throw new JED2KException(ErrorCode.BUFFER_GET_EXCEPTION);
        }
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return dst.putInt(usersCount).putInt(filesCount);
    }

    @Override
    public int bytesCount() {
        return sizeof(usersCount) + sizeof(filesCount);
    }

    @Override
    public String toString() {
        return "users: " + usersCount + " files: " + filesCount;
    }

    @Override
    public void dispatch(Dispatcher dispatcher) throws JED2KException {
        dispatcher.onServerStatus(this);
    }
}
