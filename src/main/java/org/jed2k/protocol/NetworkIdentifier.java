package org.jed2k.protocol;

import static org.jed2k.Utils.sizeof;
import static org.jed2k.Utils.int2Address;

import org.jed2k.Utils;
import org.jed2k.exception.JED2KException;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public final class NetworkIdentifier implements Serializable, Comparable<NetworkIdentifier> {
    public int ip = 0;
    public short port = 0;

    public NetworkIdentifier() {
    }

    public NetworkIdentifier(InetSocketAddress ep) {
        ip  = Utils.packToNetworkByteOrder(ep.getAddress().getAddress());
        port = (short)ep.getPort();
    }

    public NetworkIdentifier(int ip, short port) {
        this.ip = ip;
        this.port = port;
    }

    public NetworkIdentifier assign(int ip, short port) {
        this.ip = ip;
        this.port = port;
        return this;
    }

    public NetworkIdentifier assign(NetworkIdentifier point) {
        this.ip = point.ip;
        this.port = point.port;
        return this;
    }

    public final boolean defined() {
        return ip != 0 && port != 0;
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
    public int bytesCount() {
        return sizeof(ip) + sizeof(port);
    }

    @Override
    public String toString() {
        return int2Address(ip) + ":" + port;
    }

    public InetSocketAddress toInetSocketAddress() throws JED2KException {
        try {
            return new InetSocketAddress(Utils.int2Address(ip), port);
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
