package org.dkf.jed2k.protocol.kad;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.UInt32;
import org.dkf.jed2k.protocol.UInt8;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 15.11.2016.
 */
public class KadExtEntry implements Serializable {
    private UInt32 dwKey = new UInt32();
    private UInt32 dwIp = new UInt32();
    private UInt8 verified = new UInt8();

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return verified.get(dwIp.get(dwKey.get(src)));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return verified.put(dwIp.put(dwKey.put(dst)));
    }

    @Override
    public int bytesCount() {
        return dwKey.bytesCount() + dwIp.bytesCount() + verified.bytesCount();
    }

    public UInt32 getDwKey() {
        return this.dwKey;
    }

    public UInt32 getDwIp() {
        return this.dwIp;
    }

    public UInt8 getVerified() {
        return this.verified;
    }
}
