package org.dkf.jed2k.protocol.client;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.*;

import java.nio.ByteBuffer;

public class HashSetAnswer implements Serializable, Dispatchable {
    private final Hash hash = new Hash();
    private final Container<UInt16, Hash> parts = Container.makeShort(Hash.class);

    public HashSetAnswer() {
    }

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return parts.get(hash.get(src));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return parts.put(hash.put(dst));
    }

    @Override
    public int bytesCount() {
        return hash.bytesCount() + parts.bytesCount();
    }

    @Override
    public void dispatch(Dispatcher dispatcher) throws JED2KException {
        dispatcher.onClientHashSetAnswer(this);
    }

    public Hash getHash() {
        return this.hash;
    }

    public Container<UInt16, Hash> getParts() {
        return this.parts;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof HashSetAnswer)) return false;
        final HashSetAnswer other = (HashSetAnswer) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$hash = this.getHash();
        final Object other$hash = other.getHash();
        if (this$hash == null ? other$hash != null : !this$hash.equals(other$hash)) return false;
        final Object this$parts = this.getParts();
        final Object other$parts = other.getParts();
        if (this$parts == null ? other$parts != null : !this$parts.equals(other$parts)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof HashSetAnswer;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $hash = this.getHash();
        result = result * PRIME + ($hash == null ? 43 : $hash.hashCode());
        final Object $parts = this.getParts();
        result = result * PRIME + ($parts == null ? 43 : $parts.hashCode());
        return result;
    }

    public String toString() {
        return "HashSetAnswer(hash=" + this.getHash() + ", parts=" + this.getParts() + ")";
    }
}
