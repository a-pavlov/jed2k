package org.dkf.jed2k.test.kad;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Hash;
import org.dkf.jed2k.protocol.PacketCombiner;
import org.dkf.jed2k.protocol.PacketHeader;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.kad.Kad2Req;
import org.dkf.jed2k.protocol.kad.KadPacketHeader;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static junit.framework.Assert.*;

/**
 * Created by inkpot on 22.11.2016.
 */
public class PacketCombinerTest {

    @Test
    public void trivialTestKadHeader() {
        PacketHeader header = new KadPacketHeader();
        assertEquals(2, header.bytesCount());
        assertEquals(0, header.sizePacket());
        assertFalse(header.isDefined());
    }

    @Test
    public void testPackUnpack() throws JED2KException {
        ByteBuffer buffer = ByteBuffer.allocate(128);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        PacketCombiner combiner = new org.dkf.jed2k.protocol.kad.PacketCombiner();
        Kad2Req req = new Kad2Req();
        req.getReceiver().assign(Hash.EMULE);
        req.getTarget().assign(Hash.LIBED2K);
        req.setSearchType((byte)0x02);
        assertEquals(128, buffer.remaining());
        combiner.pack(req, buffer);
        assertEquals(128 - 2 - req.bytesCount(), buffer.remaining());
        PacketHeader header = new KadPacketHeader();
        buffer.flip();
        header.get(buffer);
        header.reset(header.key(), buffer.remaining());
        assertEquals(org.dkf.jed2k.protocol.kad.PacketCombiner.OP_KADEMLIAHEADER, header.key().protocol);
        assertEquals(org.dkf.jed2k.protocol.kad.PacketCombiner.KadUdp.KADEMLIA2_REQ.value, header.key().packet);
        Serializable res = combiner.unpack(header, buffer);
        assertTrue(res instanceof Kad2Req);
        assertFalse(buffer.hasRemaining());
    }
}
