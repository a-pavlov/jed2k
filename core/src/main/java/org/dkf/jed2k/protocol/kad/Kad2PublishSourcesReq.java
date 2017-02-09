package org.dkf.jed2k.protocol.kad;

import lombok.Data;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Serializable;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 19.01.2017.
 */
@Data
public class Kad2PublishSourcesReq implements Serializable {
    private KadId fileId = new KadId();
    private KadSearchEntry source = new KadSearchEntry();

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return source.get(fileId.get(src));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return source.put(fileId.put(dst));
    }

    @Override
    public int bytesCount() {
        return fileId.bytesCount() + source.bytesCount();
    }
}
