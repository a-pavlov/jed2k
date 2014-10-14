package org.jed2k.protocol;

import static org.jed2k.Utils.sizeof;

public class ServerStatus implements Serializable {
    public int usersCount = 0;
    public int filesCount = 0;
        
    @Override
    public Buffer get(Buffer src) throws ProtocolException {
        usersCount = src.getInt();
        filesCount = src.getInt();
        return src;
    }

    @Override
    public Buffer put(Buffer dst) throws ProtocolException {
        return dst.put(usersCount).put(filesCount);        
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