package org.dkf.jed2k.protocol.kad;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.UInt8;
import org.dkf.jed2k.protocol.Unsigned;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 19.01.2017.
 */
public class Kad2PublishRes implements Serializable {
    private final KadId fileId;
    private final UInt8 count = Unsigned.uint8();

    public Kad2PublishRes() {
        fileId = new KadId();
    }

    public Kad2PublishRes(final KadId id, int load) {
        fileId = id;
        count.assign(load);
    }

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return count.get(fileId.get(src));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return count.put(fileId.put(dst));
    }

    @Override
    public int bytesCount() {
        return fileId.bytesCount() + count.bytesCount();
    }

    public KadId getFileId() {
        return this.fileId;
    }

    public UInt8 getCount() {
        return this.count;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Kad2PublishRes)) return false;
        final Kad2PublishRes other = (Kad2PublishRes) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$fileId = this.getFileId();
        final Object other$fileId = other.getFileId();
        if (this$fileId == null ? other$fileId != null : !this$fileId.equals(other$fileId)) return false;
        final Object this$count = this.getCount();
        final Object other$count = other.getCount();
        if (this$count == null ? other$count != null : !this$count.equals(other$count)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Kad2PublishRes;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $fileId = this.getFileId();
        result = result * PRIME + ($fileId == null ? 43 : $fileId.hashCode());
        final Object $count = this.getCount();
        result = result * PRIME + ($count == null ? 43 : $count.hashCode());
        return result;
    }

    public String toString() {
        return "Kad2PublishRes(fileId=" + this.getFileId() + ", count=" + this.getCount() + ")";
    }
}
