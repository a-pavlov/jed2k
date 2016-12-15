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
    private KadId kid = null;
    private KadEndpoint kadEndpoint = null;
    private byte version;

    public KadEntry() {
        kid = new KadId();
        kadEndpoint = new KadEndpoint();
    }

    public KadEntry(final KadId id, final KadEndpoint endpoint, byte version) {
        kid = id;
        this.kadEndpoint = endpoint;
        this.version = version;
    }

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        version = kadEndpoint.get(kid.get(src)).get();
        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return kadEndpoint.put(kid.put(dst)).put(version);
    }

    @Override
    public int bytesCount() {
        return kid.bytesCount() + kadEndpoint.bytesCount() + 1;
    }
}
