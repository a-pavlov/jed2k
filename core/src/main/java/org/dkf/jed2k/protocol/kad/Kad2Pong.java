package org.dkf.jed2k.protocol.kad;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.UInt16;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 15.11.2016.
 */
public class Kad2Pong implements Serializable {
    private UInt16 portUdp = new UInt16();

    public Kad2Pong() {
    }

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return portUdp.get(src);
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return portUdp.put(dst);
    }

    @Override
    public int bytesCount() {
        return portUdp.bytesCount();
    }

    public UInt16 getPortUdp() {
        return this.portUdp;
    }

    public void setPortUdp(UInt16 portUdp) {
        this.portUdp = portUdp;
    }

    public String toString() {
        return "Kad2Pong(portUdp=" + this.getPortUdp() + ")";
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Kad2Pong)) return false;
        final Kad2Pong other = (Kad2Pong) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$portUdp = this.getPortUdp();
        final Object other$portUdp = other.getPortUdp();
        if (this$portUdp == null ? other$portUdp != null : !this$portUdp.equals(other$portUdp)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Kad2Pong;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $portUdp = this.getPortUdp();
        result = result * PRIME + ($portUdp == null ? 43 : $portUdp.hashCode());
        return result;
    }
}
