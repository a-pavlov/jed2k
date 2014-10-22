package org.jed2k.protocol;

import static org.jed2k.Utils.sizeof;
import static org.jed2k.Utils.int2Address;

import org.jed2k.exception.JED2KException;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public final class NetworkIdentifier implements Serializable, Comparable<NetworkIdentifier> {
    private int ip = 0;
    private short port = 0;

    public NetworkIdentifier() {
    }
    
    public NetworkIdentifier(int ip, short port) {
        this.ip = ip;
        this.port = port;
    }
    
    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        ip = src.getInt();
        port = src.getShort();
        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return dst.putInt(ip).putShort(port);
    }
    
    @Override
    public int size() {
        return sizeof(ip) + sizeof(port);
    }
    
    @Override
    public String toString() {
        return int2Address(ip) + ":" + port(); 
    }
    
    public void reset(int ip, short port) {
        this.ip = ip;
        this.port = port;
    }
    
    public int ip() {
        return ip;
    }
    
    public int port() {
        return (port & 0xffff);
    }
        
    public InetSocketAddress toInetSocketAddress() throws JED2KException {
        try {
            return new InetSocketAddress(int2Address(ip), port);
        } catch(IllegalArgumentException e) {
            throw new JED2KException(e);
        }
    }

    @Override
    public int compareTo(NetworkIdentifier arg0) {
        if (ip > arg0.ip) return 1;
        if (ip < arg0.ip) return -1;
        if (port > arg0.port) return 1;
        if (port < arg0.port) return -1;
        return 0;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof NetworkIdentifier) {
            if (compareTo((NetworkIdentifier)o) == 0) {
                return true;
            }
        }
        
        return false;
    }
}
