package org.dkf.jed2k.protocol;

import org.dkf.jed2k.Utils;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.dkf.jed2k.Utils.int2Address;
import static org.dkf.jed2k.Utils.sizeof;

public final class NetworkIdentifier implements Serializable, Comparable<NetworkIdentifier> {
    private int ip = 0;
    private int port = 0;

    /**
     * for serialization purposes
     */
    public NetworkIdentifier() {
    }

    public NetworkIdentifier(InetSocketAddress ep) {
        ip  = Utils.packToNetworkByteOrder(ep.getAddress().getAddress());
        port = (short)ep.getPort();
    }

    public NetworkIdentifier(int ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public NetworkIdentifier assign(int ip, int port) {
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
        assert(src.order() == ByteOrder.LITTLE_ENDIAN);
        ip = src.getInt();
        UInt16 p = new UInt16(0);
        p.get(src);
        port = p.intValue();
        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        assert(dst.order() == ByteOrder.LITTLE_ENDIAN);
        return dst.putInt(ip).putShort((short)port);
    }

    @Override
    public int bytesCount() {
        return sizeof(ip) + sizeof(port) / 2;
    }

    @Override
    public String toString() {
        return int2Address(ip) + ":" + port;
    }

    public InetSocketAddress toInetSocketAddress() throws JED2KException {
        try {
            return new InetSocketAddress(Utils.int2Address(ip), port);
        } catch(IllegalArgumentException e) {
            throw new JED2KException(e, ErrorCode.ILLEGAL_ARGUMENT);
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
        if (o instanceof NetworkIdentifier && compareTo((NetworkIdentifier)o) == 0) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return ip + port;
    }

    public void setIP(int i) {
        ip = i;
    }

    public void setPort(short p) {
        port = p;
    }

    public int getIP() { return ip; }
    public int getPort() { return port; }
}
