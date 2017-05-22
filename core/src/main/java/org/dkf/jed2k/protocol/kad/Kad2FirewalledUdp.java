package org.dkf.jed2k.protocol.kad;

import org.dkf.jed2k.Utils;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.UInt16;
import org.dkf.jed2k.protocol.Unsigned;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * Created by inkpot on 16.12.2016.
 */
public class Kad2FirewalledUdp implements Serializable {
    byte errorCode;
    UInt16 portTcp = Unsigned.uint16();

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        try {
            errorCode = src.get();
        } catch(BufferUnderflowException e) {
            throw new JED2KException(ErrorCode.BUFFER_UNDERFLOW_EXCEPTION);
        } catch(Exception e) {
            throw new JED2KException(ErrorCode.BUFFER_GET_EXCEPTION);
        }
        return portTcp.get(src);
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return portTcp.put(dst.put(errorCode));
    }

    @Override
    public int bytesCount() {
        return Utils.sizeof(errorCode) + portTcp.bytesCount();
    }
}
