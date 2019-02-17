package org.dkf.jed2k.protocol.kad;

import org.dkf.jed2k.Utils;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Serializable;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * Created by inkpot on 22.01.2017.
 */
public class Kad2FirewalledRes implements Serializable {
    private int ip;

    public Kad2FirewalledRes() {
    }

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        try {
            ip = Utils.ntohl(src.getInt());
        } catch(BufferUnderflowException e) {
            throw new JED2KException(ErrorCode.BUFFER_UNDERFLOW_EXCEPTION);
        } catch(Exception e) {
            throw new JED2KException(ErrorCode.BUFFER_GET_EXCEPTION);
        }
        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return dst.putInt(Utils.ntohl(ip));
    }

    @Override
    public int bytesCount() {
        return Utils.sizeof(ip);
    }

    public int getIp() {
        return this.ip;
    }

    public void setIp(int ip) {
        this.ip = ip;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Kad2FirewalledRes)) return false;
        final Kad2FirewalledRes other = (Kad2FirewalledRes) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.getIp() != other.getIp()) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Kad2FirewalledRes;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getIp();
        return result;
    }

    public String toString() {
        return "Kad2FirewalledRes(ip=" + this.getIp() + ")";
    }
}
