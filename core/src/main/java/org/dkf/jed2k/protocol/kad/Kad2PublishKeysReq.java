package org.dkf.jed2k.protocol.kad;

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
public class Kad2PublishKeysReq implements Serializable, KadDispatchable {
    private KadId keywordId = new KadId();
    private Container<UInt16, KadSearchEntry> sources = Container.makeShort(KadSearchEntry.class);

    public Kad2PublishKeysReq() {
    }

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

    public KadId getKeywordId() {
        return this.keywordId;
    }

    public Container<UInt16, KadSearchEntry> getSources() {
        return this.sources;
    }

    public void setKeywordId(KadId keywordId) {
        this.keywordId = keywordId;
    }

    public void setSources(Container<UInt16, KadSearchEntry> sources) {
        this.sources = sources;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Kad2PublishKeysReq)) return false;
        final Kad2PublishKeysReq other = (Kad2PublishKeysReq) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$keywordId = this.getKeywordId();
        final Object other$keywordId = other.getKeywordId();
        if (this$keywordId == null ? other$keywordId != null : !this$keywordId.equals(other$keywordId)) return false;
        final Object this$sources = this.getSources();
        final Object other$sources = other.getSources();
        if (this$sources == null ? other$sources != null : !this$sources.equals(other$sources)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Kad2PublishKeysReq;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $keywordId = this.getKeywordId();
        result = result * PRIME + ($keywordId == null ? 43 : $keywordId.hashCode());
        final Object $sources = this.getSources();
        result = result * PRIME + ($sources == null ? 43 : $sources.hashCode());
        return result;
    }

    public String toString() {
        return "Kad2PublishKeysReq(keywordId=" + this.getKeywordId() + ", sources=" + this.getSources() + ")";
    }
}
