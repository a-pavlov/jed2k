package org.jed2k.protocol;

import static org.jed2k.Utils.sizeof;
import java.nio.ByteBuffer;
import org.jed2k.exception.JED2KException;

public class ServerStatus implements Serializable, Dispatchable {
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
    public boolean dispatch(Dispatcher dispatcher) {
        return dispatcher.onServerStatus(this);
    }
}
