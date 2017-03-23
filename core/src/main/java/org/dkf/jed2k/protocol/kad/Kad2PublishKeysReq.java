package org.dkf.jed2k.protocol.kad;

import lombok.Data;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.kad.ReqDispatcher;
import org.dkf.jed2k.protocol.Container;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.UInt16;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * Created by inkpot on 19.01.2017.
 */
@Data
public class Kad2PublishKeysReq implements Serializable, KadDispatchable {
    private KadId keywordId = new KadId();
    private Container<UInt16, KadSearchEntry> sources = Container.makeShort(KadSearchEntry.class);

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return sources.get(keywordId.get(src));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return sources.put(keywordId.put(dst));
    }

    @Override
    public int bytesCount() {
        return keywordId.bytesCount() + sources.bytesCount();
    }

    @Override
    public void dispatch(ReqDispatcher dispatcher, final InetSocketAddress address) {
        dispatcher.process(this, address);
    }
}
