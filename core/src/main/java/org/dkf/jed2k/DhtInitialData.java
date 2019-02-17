package org.dkf.jed2k;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.kad.NodeEntry;
import org.dkf.jed2k.protocol.Container;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.UInt32;
import org.dkf.jed2k.protocol.kad.KadId;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 18.12.2016.
 */
public class DhtInitialData implements Serializable {
    private KadId target = new KadId();
    private Container<UInt32, NodeEntry> entries = Container.makeInt(NodeEntry.class);

    public DhtInitialData() {
    }

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return entries.get(target.get(src));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return entries.put(target.put(dst));
    }

    @Override
    public int bytesCount() {
        return target.bytesCount() + entries.bytesCount();
    }

    public KadId getTarget() {
        return this.target;
    }

    public Container<UInt32, NodeEntry> getEntries() {
        return this.entries;
    }

    public void setTarget(KadId target) {
        this.target = target;
    }

    public void setEntries(Container<UInt32, NodeEntry> entries) {
        this.entries = entries;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof DhtInitialData)) return false;
        final DhtInitialData other = (DhtInitialData) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$target = this.getTarget();
        final Object other$target = other.getTarget();
        if (this$target == null ? other$target != null : !this$target.equals(other$target)) return false;
        final Object this$entries = this.getEntries();
        final Object other$entries = other.getEntries();
        if (this$entries == null ? other$entries != null : !this$entries.equals(other$entries)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof DhtInitialData;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $target = this.getTarget();
        result = result * PRIME + ($target == null ? 43 : $target.hashCode());
        final Object $entries = this.getEntries();
        result = result * PRIME + ($entries == null ? 43 : $entries.hashCode());
        return result;
    }

    public String toString() {
        return "DhtInitialData(target=" + this.getTarget() + ", entries=" + this.getEntries() + ")";
    }
};
