package org.dkf.jed2k.protocol.kad;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.kad.ReqDispatcher;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.UInt16;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * Created by inkpot on 15.11.2016.
 */
public class Kad2SearchKeysReq implements Serializable, KadDispatchable {
    private KadId target = new KadId();
    private UInt16 startPos = new UInt16(0);

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return startPos.get(target.get(src));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return startPos.put(target.put(dst));
    }

    @Override
    public int bytesCount() {
        return target.bytesCount() + startPos.bytesCount();
    }

    @Override
    public void dispatch(ReqDispatcher dispatcher, final InetSocketAddress address) {
        dispatcher.process(this, address);
    }

    public KadId getTarget() {
        return this.target;
    }

    public UInt16 getStartPos() {
        return this.startPos;
    }

    public void setTarget(KadId target) {
        this.target = target;
    }

    public void setStartPos(UInt16 startPos) {
        this.startPos = startPos;
    }
}
