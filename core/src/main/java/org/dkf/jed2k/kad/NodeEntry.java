package org.dkf.jed2k.kad;

import org.dkf.jed2k.Time;
import org.dkf.jed2k.protocol.Hash;
import org.dkf.jed2k.protocol.kad.KadId;

import java.net.InetSocketAddress;

/**
 * Created by inkpot on 23.11.2016.
 */
public class NodeEntry {
    private InetSocketAddress address;
    private KadId id;
    private int timeoutCount;
    private long firstSeen;

    public NodeEntry(final KadId id_, final InetSocketAddress address, boolean pinged) {
        this.id = id_;
        this.address = address;
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

    public int failCount() { return isPinged() ? timeoutCount : 0; }
    public void resetFailCount() { if (isPinged()) timeoutCount = 0; }
    public InetSocketAddress getEndpoint() { return address; }
    public boolean isConfirmed() { return timeoutCount == 0; }
}
