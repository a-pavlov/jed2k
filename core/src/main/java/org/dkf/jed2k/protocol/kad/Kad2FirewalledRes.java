package org.dkf.jed2k.protocol.kad;

import lombok.Data;
import org.dkf.jed2k.Utils;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Serializable;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * Created by inkpot on 22.01.2017.
 */
@Data
public class Kad2FirewalledRes implements Serializable {
    private int ip;

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
}
