package org.dkf.jed2k.protocol.kad;

import lombok.Data;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Serializable;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 15.11.2016.
 */
@Data
public class KadEntry implements Serializable {
    private KadId kid = new KadId();
    private KadNetworkIdentifier endpoint = new KadNetworkIdentifier();
    private byte version;

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        version = endpoint.get(kid.get(src)).get();
        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return endpoint.put(kid.put(dst)).put(version);
    }

    @Override
    public int bytesCount() {
        return kid.bytesCount() + endpoint.bytesCount() + 1;
    }
}
