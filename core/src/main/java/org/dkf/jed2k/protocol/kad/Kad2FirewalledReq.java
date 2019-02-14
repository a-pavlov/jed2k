package org.dkf.jed2k.protocol.kad;

import org.dkf.jed2k.Utils;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.kad.ReqDispatcher;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.UInt16;
import org.dkf.jed2k.protocol.Unsigned;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * Created by inkpot on 16.12.2016.
 */
public class Kad2FirewalledReq implements Serializable, KadDispatchable {
    UInt16 portTcp = Unsigned.uint16();
    KadId id = new KadId();
    byte options;

    public Kad2FirewalledReq() {
    }

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        options = id.get(portTcp.get(src)).get();
        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return id.put(portTcp.put(dst)).put(options);
    }

    @Override
    public int bytesCount() {
        return portTcp.bytesCount() + id.bytesCount() + Utils.sizeof(options);
    }

    @Override
    public void dispatch(ReqDispatcher dispatcher, final InetSocketAddress address) {
        dispatcher.process(this, address);
    }

    public UInt16 getPortTcp() {
        return this.portTcp;
    }

    public KadId getId() {
        return this.id;
    }

    public byte getOptions() {
        return this.options;
    }

    public void setPortTcp(UInt16 portTcp) {
        this.portTcp = portTcp;
    }

    public void setId(KadId id) {
        this.id = id;
    }

    public void setOptions(byte options) {
        this.options = options;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Kad2FirewalledReq)) return false;
        final Kad2FirewalledReq other = (Kad2FirewalledReq) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$portTcp = this.getPortTcp();
        final Object other$portTcp = other.getPortTcp();
        if (this$portTcp == null ? other$portTcp != null : !this$portTcp.equals(other$portTcp)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        if (this.getOptions() != other.getOptions()) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Kad2FirewalledReq;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $portTcp = this.getPortTcp();
        result = result * PRIME + ($portTcp == null ? 43 : $portTcp.hashCode());
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        result = result * PRIME + this.getOptions();
        return result;
    }

    public String toString() {
        return "Kad2FirewalledReq(portTcp=" + this.getPortTcp() + ", id=" + this.getId() + ", options=" + this.getOptions() + ")";
    }
}
