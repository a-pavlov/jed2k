package org.dkf.jed2k.kad;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
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
@Getter
@ToString
@EqualsAndHashCode(exclude = {"timeoutCount", "firstSeen"})
public class NodeEntry implements Serializable {
    private KadId id;
    private Endpoint endpoint;
    private int timeoutCount;
    private long firstSeen;

    public NodeEntry() {
        id = new KadId();
        endpoint = new Endpoint(0, 0);
    }

    public NodeEntry(final KadId id_, final Endpoint address, boolean pinged) {
        this.id = id_;
        this.endpoint = address;
        this.timeoutCount = (pinged)?0:0xffff;
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
}
