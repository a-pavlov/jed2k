package org.dkf.jed2k.protocol.kad;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.kad.ReqDispatcher;
import org.dkf.jed2k.protocol.Serializable;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * Created by inkpot on 19.01.2017.
 */
public class Kad2PublishSourcesReq implements Serializable, KadDispatchable {
    private KadId fileId = new KadId();
    private KadSearchEntry source = new KadSearchEntry();

    public Kad2PublishSourcesReq() {
    }

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

    @Override
    public void dispatch(ReqDispatcher dispatcher, final InetSocketAddress address) {
        dispatcher.process(this, address);
    }

    public KadId getFileId() {
        return this.fileId;
    }

    public KadSearchEntry getSource() {
        return this.source;
    }

    public void setFileId(KadId fileId) {
        this.fileId = fileId;
    }

    public void setSource(KadSearchEntry source) {
        this.source = source;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Kad2PublishSourcesReq)) return false;
        final Kad2PublishSourcesReq other = (Kad2PublishSourcesReq) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$fileId = this.getFileId();
        final Object other$fileId = other.getFileId();
        if (this$fileId == null ? other$fileId != null : !this$fileId.equals(other$fileId)) return false;
        final Object this$source = this.getSource();
        final Object other$source = other.getSource();
        if (this$source == null ? other$source != null : !this$source.equals(other$source)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Kad2PublishSourcesReq;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $fileId = this.getFileId();
        result = result * PRIME + ($fileId == null ? 43 : $fileId.hashCode());
        final Object $source = this.getSource();
        result = result * PRIME + ($source == null ? 43 : $source.hashCode());
        return result;
    }

    public String toString() {
        return "Kad2PublishSourcesReq(fileId=" + this.getFileId() + ", source=" + this.getSource() + ")";
    }
}
