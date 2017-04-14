package org.dkf.jed2k.kad.server;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.kad.Kad2SearchRes;
import org.dkf.jed2k.protocol.kad.KadId;
import org.dkf.jed2k.protocol.kad.KadPacketHeader;
import org.dkf.jed2k.protocol.kad.PacketCombiner;

import java.nio.ByteBuffer;

/**
 * Created by apavlov on 14.04.17.
 */
public class Kad2SearchResHeader implements Serializable {
    private final KadPacketHeader header;
    private final KadId source;
    private final KadId target;
    private static PacketCombiner pc = new PacketCombiner();

    public Kad2SearchResHeader(final KadId source, final KadId target) {
        header = new KadPacketHeader();
        header.reset(pc.classToKey(Kad2SearchRes.class), 0);
        this.source = source;
        this.target = target;
    }

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return target.get(source.get(header.get(src)));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return target.put(source.put(header.put(dst)));
    }

    @Override
    public int bytesCount() {
        return header.bytesCount() + source.bytesCount() + target.bytesCount();
    }
}
