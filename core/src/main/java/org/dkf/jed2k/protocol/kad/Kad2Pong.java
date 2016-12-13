package org.dkf.jed2k.protocol.kad;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.UInt16;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 15.11.2016.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Kad2Pong extends Transaction {
    private UInt16 portUdp = new UInt16();

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
}
