package org.dkf.jed2k.protocol.kad;

import org.dkf.jed2k.Utils;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.UInt16;
import org.dkf.jed2k.protocol.UInt32;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 15.11.2016.
 */
public class KadEndpoint implements Serializable {
    private UInt32 ip = new UInt32();
    private UInt16 portUdp = new UInt16();
    private UInt16 portTcp = new UInt16();

    public KadEndpoint() {

    }

    public KadEndpoint(int ip, int port, int portTcp) {
        this.ip.assign(ip);
        this.portUdp.assign(port);
        this.portTcp.assign(portTcp);
    }

    public Endpoint getEndpoint() {
        return new Endpoint(ip.intValue(), portUdp.intValue());
    }

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        portTcp.get(portUdp.get(ip.get(src)));
        ip.assign(Utils.ntohl(ip.intValue()));
        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return portTcp.put(portUdp.put(dst.putInt(Utils.htonl(ip.intValue()))));
    }

    @Override
    public int bytesCount() {
        return ip.bytesCount() + portUdp.bytesCount() + portTcp.bytesCount();
    }

    public UInt32 getIp() {
        return this.ip;
    }

    public UInt16 getPortUdp() {
        return this.portUdp;
    }

    public UInt16 getPortTcp() {
        return this.portTcp;
    }

    public void setIp(UInt32 ip) {
        this.ip = ip;
    }

    public void setPortUdp(UInt16 portUdp) {
        this.portUdp = portUdp;
    }

    public void setPortTcp(UInt16 portTcp) {
        this.portTcp = portTcp;
    }

    public String toString() {
        return "KadEndpoint(ip=" + this.getIp() + ", portUdp=" + this.getPortUdp() + ", portTcp=" + this.getPortTcp() + ")";
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof KadEndpoint)) return false;
        final KadEndpoint other = (KadEndpoint) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$ip = this.getIp();
        final Object other$ip = other.getIp();
        if (this$ip == null ? other$ip != null : !this$ip.equals(other$ip)) return false;
        final Object this$portUdp = this.getPortUdp();
        final Object other$portUdp = other.getPortUdp();
        if (this$portUdp == null ? other$portUdp != null : !this$portUdp.equals(other$portUdp)) return false;
        final Object this$portTcp = this.getPortTcp();
        final Object other$portTcp = other.getPortTcp();
        if (this$portTcp == null ? other$portTcp != null : !this$portTcp.equals(other$portTcp)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof KadEndpoint;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $ip = this.getIp();
        result = result * PRIME + ($ip == null ? 43 : $ip.hashCode());
        final Object $portUdp = this.getPortUdp();
        result = result * PRIME + ($portUdp == null ? 43 : $portUdp.hashCode());
        final Object $portTcp = this.getPortTcp();
        result = result * PRIME + ($portTcp == null ? 43 : $portTcp.hashCode());
        return result;
    }
}
