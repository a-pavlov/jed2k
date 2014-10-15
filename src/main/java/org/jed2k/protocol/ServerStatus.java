package org.jed2k.protocol;

import static org.jed2k.Utils.sizeof;

import java.nio.ByteBuffer;

public class ServerStatus implements Serializable {
    public int usersCount = 0;
    public int filesCount = 0;
        
    @Override
    public ByteBuffer get(ByteBuffer src) throws ProtocolException {
        usersCount = src.getInt();
        filesCount = src.getInt();
        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws ProtocolException {
        return dst.putInt(usersCount).putInt(filesCount);        
    }

    @Override
    public int size() {
        return sizeof(usersCount) + sizeof(filesCount);
    }
    
    @Override
    public String toString() {
        return "users: " + usersCount + " files: " + filesCount;  
    }
}