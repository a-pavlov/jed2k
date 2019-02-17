package org.dkf.jed2k.protocol.kad;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Container;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.UInt16;
import org.dkf.jed2k.protocol.UInt8;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 15.11.2016.
 */
public class Kad2BootstrapRes implements Serializable {
    private KadId kid = new KadId();
    private UInt16 portTcp = new UInt16();
    private UInt8 version = new UInt8();
    private Container<UInt16, KadEntry> contacts = Container.makeShort(KadEntry.class);

    public Kad2BootstrapRes() {
    }

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return contacts.get(version.get(portTcp.get(kid.get(src))));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return contacts.put(version.put(portTcp.put(kid.put(dst))));
    }

    @Override
    public int bytesCount() {
        return kid.bytesCount() + portTcp.bytesCount() + version.bytesCount() + contacts.bytesCount();
    }

    public KadId getKid() {
        return this.kid;
    }

    public UInt16 getPortTcp() {
        return this.portTcp;
    }

    public UInt8 getVersion() {
        return this.version;
    }

    public Container<UInt16, KadEntry> getContacts() {
        return this.contacts;
    }

    public void setKid(KadId kid) {
        this.kid = kid;
    }

    public void setPortTcp(UInt16 portTcp) {
        this.portTcp = portTcp;
    }

    public void setVersion(UInt8 version) {
        this.version = version;
    }

    public void setContacts(Container<UInt16, KadEntry> contacts) {
        this.contacts = contacts;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Kad2BootstrapRes)) return false;
        final Kad2BootstrapRes other = (Kad2BootstrapRes) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$kid = this.getKid();
        final Object other$kid = other.getKid();
        if (this$kid == null ? other$kid != null : !this$kid.equals(other$kid)) return false;
        final Object this$portTcp = this.getPortTcp();
        final Object other$portTcp = other.getPortTcp();
        if (this$portTcp == null ? other$portTcp != null : !this$portTcp.equals(other$portTcp)) return false;
        final Object this$version = this.getVersion();
        final Object other$version = other.getVersion();
        if (this$version == null ? other$version != null : !this$version.equals(other$version)) return false;
        final Object this$contacts = this.getContacts();
        final Object other$contacts = other.getContacts();
        if (this$contacts == null ? other$contacts != null : !this$contacts.equals(other$contacts)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Kad2BootstrapRes;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $kid = this.getKid();
        result = result * PRIME + ($kid == null ? 43 : $kid.hashCode());
        final Object $portTcp = this.getPortTcp();
        result = result * PRIME + ($portTcp == null ? 43 : $portTcp.hashCode());
        final Object $version = this.getVersion();
        result = result * PRIME + ($version == null ? 43 : $version.hashCode());
        final Object $contacts = this.getContacts();
        result = result * PRIME + ($contacts == null ? 43 : $contacts.hashCode());
        return result;
    }

    public String toString() {
        return "Kad2BootstrapRes(kid=" + this.getKid() + ", portTcp=" + this.getPortTcp() + ", version=" + this.getVersion() + ", contacts=" + this.getContacts() + ")";
    }
}
