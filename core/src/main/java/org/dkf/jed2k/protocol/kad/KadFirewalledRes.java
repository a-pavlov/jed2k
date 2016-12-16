package org.dkf.jed2k.protocol.kad;

import org.dkf.jed2k.Utils;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Serializable;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 16.12.2016.
 */
public class KadFirewalledRes implements Serializable {
    private int address;

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        address = src.getInt();
        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return dst.putInt(address);
    }

    @Override
    public int bytesCount() {
        return Utils.sizeof(address);
    }
}
