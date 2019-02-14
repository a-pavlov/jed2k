package org.dkf.jed2k.kad;

import org.dkf.jed2k.Time;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.Hash;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.kad.KadId;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * Created by inkpot on 23.11.2016.
 */
public class NodeEntry implements Serializable {
    private KadId id;
    private Endpoint endpoint;
    private int portTcp = 0;
    private byte version = 0;
    private int timeoutCount;
    private long firstSeen;

    public NodeEntry() {
        id = new KadId();
        endpoint = new Endpoint(0, 0);
    }

    public NodeEntry(final KadId id_, final Endpoint address, boolean pinged, int portTcp, byte version) {
        this.id = id_;
        this.endpoint = address;
        this.timeoutCount = (pinged)?0:0xffff;
        this.portTcp = portTcp;
        this.version = version;
        firstSeen = Time.currentTime();
    }

    public NodeEntry(final InetSocketAddress address) {
        id = new KadId(Hash.INVALID);
        this.timeoutCount = 0xffff;
        this.firstSeen = Time.currentTime();
    }

    public boolean isPinged() { return timeoutCount != 0xffff; }
    public void setPinged() { if (timeoutCount == 0xffff) timeoutCount = 0; }

    public void timedOut() { if (isPinged()) ++timeoutCount; }
    public void setTimeoutCount(int value) {
        timeoutCount = value;
    }

    public int failCount() { return isPinged() ? timeoutCount : 0; }
    public void resetFailCount() { if (isPinged()) timeoutCount = 0; }
    public boolean isConfirmed() { return timeoutCount == 0; }

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return endpoint.get(id.get(src));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return endpoint.put(id.put(dst));
    }

    @Override
    public int bytesCount() {
        return id.bytesCount() + endpoint.bytesCount();
    }

    public void setPortTcp(int port) {
        portTcp = port;
    }

    public void setVersion(byte v) {
        version = v;
    }

    public KadId getId() {
        return this.id;
    }

    public Endpoint getEndpoint() {
        return this.endpoint;
    }

    public int getPortTcp() {
        return this.portTcp;
    }

    public byte getVersion() {
        return this.version;
    }

    public int getTimeoutCount() {
        return this.timeoutCount;
    }

    public long getFirstSeen() {
        return this.firstSeen;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof NodeEntry)) return false;
        final NodeEntry other = (NodeEntry) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$endpoint = this.getEndpoint();
        final Object other$endpoint = other.getEndpoint();
        if (this$endpoint == null ? other$endpoint != null : !this$endpoint.equals(other$endpoint)) return false;
        if (this.getPortTcp() != other.getPortTcp()) return false;
        if (this.getVersion() != other.getVersion()) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof NodeEntry;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $endpoint = this.getEndpoint();
        result = result * PRIME + ($endpoint == null ? 43 : $endpoint.hashCode());
        result = result * PRIME + this.getPortTcp();
        result = result * PRIME + this.getVersion();
        return result;
    }

    public String toString() {
        return "NodeEntry(id=" + this.getId() + ", endpoint=" + this.getEndpoint() + ", portTcp=" + this.getPortTcp() + ", version=" + this.getVersion() + ", timeoutCount=" + this.getTimeoutCount() + ", firstSeen=" + this.getFirstSeen() + ")";
    }
}
