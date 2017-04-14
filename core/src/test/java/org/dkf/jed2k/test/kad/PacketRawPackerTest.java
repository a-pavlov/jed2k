package org.dkf.jed2k.test.kad;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.kad.server.Kad2SearchResHeader;
import org.dkf.jed2k.kad.server.PacketRawPacker;
import org.dkf.jed2k.protocol.*;
import org.dkf.jed2k.protocol.kad.KadId;
import org.dkf.jed2k.protocol.kad.KadPacketHeader;
import org.dkf.jed2k.protocol.kad.PacketCombiner;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.Assert.*;

/**
 * Created by apavlov on 10.04.17.
 */
public class PacketRawPackerTest {
    private PacketCombiner pc = new PacketCombiner();
    private Serializable kph = new Kad2SearchResHeader(new KadId(Hash.EMULE), new KadId(Hash.LIBED2K));

    @Test
    public void testPacking() throws JED2KException {
        ByteBuffer buff = ByteBuffer.allocate(100);
        buff.order(ByteOrder.LITTLE_ENDIAN);
        PacketRawPacker prp = new PacketRawPacker(buff, kph);
        assertTrue(prp.isEmpty());

        Endpoint e = new Endpoint(1001,1010);
        int capacity = (100 - kph.bytesCount() - 2) / e.bytesCount();
        assertTrue(capacity > 0);

        ByteBuffer bb = ByteBuffer.allocate(100);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        byte[] data = new byte[e.bytesCount()];
        for(int i = 0; i < capacity; ++i) {
            e.setPort(i+1000);
            e.setIP(i+2000);
            e.put(bb);
            bb.flip();
            bb.get(data);
            bb.clear();
            assertEquals(data.length, e.bytesCount());
            assertTrue(data.length > 0);
            assertTrue(prp.hasSpace(data.length));
            prp.putBlock(data);
        }

        assertFalse(prp.hasSpace(e.bytesCount()));
        prp.releaseBuffer();
        Container<UInt16, Endpoint> x = Container.makeShort(Endpoint.class);
        buff.clear();
        buff.position(kph.bytesCount());
        x.get(buff);
        assertEquals(capacity, x.size());
        int port = 1000;
        int ip = 2000;
        for(final Endpoint ep: x) {
            assertEquals(new Endpoint(ip++, port++), ep);
        }

        assertTrue(prp.isEmpty());
        buff.clear();
        // fill buffer again
        for(int i = 0; i < capacity; ++i) {
            e.setPort(i+4000);
            e.setIP(i+5000);
            e.put(bb);
            bb.flip();
            bb.get(data);
            bb.clear();
            assertEquals(data.length, e.bytesCount());
            assertTrue(data.length > 0);
            assertTrue(prp.hasSpace(data.length));
            prp.putBlock(data);
        }

        assertFalse(prp.isEmpty());
        assertFalse(prp.hasSpace(e.bytesCount()));
        prp.releaseBuffer();
        Container<UInt16, Endpoint> x2 = Container.makeShort(Endpoint.class);
        buff.clear();
        buff.position(kph.bytesCount());
        x2.get(buff);
        assertEquals(capacity, x2.size());
        port = 4000;
        ip = 5000;
        for(final Endpoint ep: x2) {
            assertEquals(new Endpoint(ip++, port++), ep);
        }

        assertTrue(prp.isEmpty());
    }
}
