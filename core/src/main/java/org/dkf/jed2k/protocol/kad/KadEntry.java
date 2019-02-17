package org.dkf.jed2k.protocol.kad;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Serializable;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 15.11.2016.
 */
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

    public KadId getKid() {
        return this.kid;
    }

    public KadEndpoint getKadEndpoint() {
        return this.kadEndpoint;
    }

    public byte getVersion() {
        return this.version;
    }

    public void setKid(KadId kid) {
        this.kid = kid;
    }

    public void setKadEndpoint(KadEndpoint kadEndpoint) {
        this.kadEndpoint = kadEndpoint;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof KadEntry)) return false;
        final KadEntry other = (KadEntry) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$kid = this.getKid();
        final Object other$kid = other.getKid();
        if (this$kid == null ? other$kid != null : !this$kid.equals(other$kid)) return false;
        final Object this$kadEndpoint = this.getKadEndpoint();
        final Object other$kadEndpoint = other.getKadEndpoint();
        if (this$kadEndpoint == null ? other$kadEndpoint != null : !this$kadEndpoint.equals(other$kadEndpoint))
            return false;
        if (this.getVersion() != other.getVersion()) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof KadEntry;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $kid = this.getKid();
        result = result * PRIME + ($kid == null ? 43 : $kid.hashCode());
        final Object $kadEndpoint = this.getKadEndpoint();
        result = result * PRIME + ($kadEndpoint == null ? 43 : $kadEndpoint.hashCode());
        result = result * PRIME + this.getVersion();
        return result;
    }

    public String toString() {
        return "KadEntry(kid=" + this.getKid() + ", kadEndpoint=" + this.getKadEndpoint() + ", version=" + this.getVersion() + ")";
    }
}
