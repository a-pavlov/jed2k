package org.dkf.jed2k.protocol.kad;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Container;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.UInt8;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 21.11.2016.
 */
public class Kad2Res implements Serializable {
    private KadId target = new KadId();
    private Container<UInt8, KadEntry> results = Container.makeByte(KadEntry.class);

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return results.get(target.get(src));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return results.put(target.put(dst));
    }

    @Override
    public int bytesCount() {
        return target.bytesCount() + results.bytesCount();
    }

    public KadId getTarget() {
        return this.target;
    }

    public Container<UInt8, KadEntry> getResults() {
        return this.results;
    }

    public void setTarget(KadId target) {
        this.target = target;
    }

    public void setResults(Container<UInt8, KadEntry> results) {
        this.results = results;
    }

    public String toString() {
        return "Kad2Res(target=" + this.getTarget() + ", results=" + this.getResults() + ")";
    }

    // TODO - move to rpc manager
    //@Override
    //public KadId getTargetId() {
    //    return target;
    //}
}
