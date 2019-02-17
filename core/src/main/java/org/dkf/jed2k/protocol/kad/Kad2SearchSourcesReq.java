package org.dkf.jed2k.protocol.kad;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.kad.ReqDispatcher;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.UInt16;
import org.dkf.jed2k.protocol.UInt64;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * Created by inkpot on 15.11.2016.
 */
public class Kad2SearchSourcesReq implements Serializable, KadDispatchable {
    private KadId target = new KadId();
    private UInt16 startPos = new UInt16();
    private UInt64 size = new UInt64();

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return size.get(startPos.get(target.get(src)));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return size.put(startPos.put(target.put(dst)));
    }

    @Override
    public int bytesCount() {
        return target.bytesCount() + startPos.bytesCount() + size.bytesCount();
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

    public UInt64 getSize() {
        return this.size;
    }

    public void setTarget(KadId target) {
        this.target = target;
    }

    public void setStartPos(UInt16 startPos) {
        this.startPos = startPos;
    }

    public void setSize(UInt64 size) {
        this.size = size;
    }

    // TODO - move to rpc manager
    //@Override
    //public KadId getTargetId() {
    //    return target;
    //}
}
