package org.dkf.jed2k.protocol.kad;

import lombok.Data;
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
@Data
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
}
