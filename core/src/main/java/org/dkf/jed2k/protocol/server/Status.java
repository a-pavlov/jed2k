package org.dkf.jed2k.protocol.server;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Dispatchable;
import org.dkf.jed2k.protocol.Dispatcher;
import org.dkf.jed2k.protocol.Serializable;

import java.nio.ByteBuffer;

import static org.dkf.jed2k.Utils.sizeof;

public class Status implements Serializable, Dispatchable {
    public int usersCount = 0;
    public int filesCount = 0;

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        usersCount = src.getInt();
        filesCount = src.getInt();
        return src;
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
