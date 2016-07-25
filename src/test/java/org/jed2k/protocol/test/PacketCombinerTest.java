package org.jed2k.protocol.test;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertEquals;
import static org.jed2k.protocol.tag.Tag.tag;
import static org.junit.Assert.assertFalse;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;
import java.util.LinkedList;

import org.jed2k.protocol.*;
import org.jed2k.protocol.PacketCombiner;
import org.junit.Assert;
import org.junit.Test;
import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.tag.Tag;

import org.jed2k.protocol.server.*;

public class PacketCombinerTest {

    // packet 1
    private static LoginRequest login = new LoginRequest();
    private static int version = 10;
    private static int capability = 20;
    private static int versionClient = 109;

    static {
        login.hash = Hash.EMULE;
        login.properties.add(tag(Tag.CT_VERSION, null, version));
        login.properties.add(tag(Tag.CT_SERVER_FLAGS, null, capability));
        try {
            login.properties.add(tag(Tag.CT_NAME, null, "jed2k"));
        } catch(JED2KException e) {

        }
        login.properties.add(tag(Tag.CT_EMULE_VERSION, null, versionClient));
    }

    @Test
    public void testPackUnpack() throws JED2KException {
        ByteBuffer nb = ByteBuffer.allocate(1024);
        nb.order(ByteOrder.LITTLE_ENDIAN);
        PacketCombiner combiner = new org.jed2k.protocol.server.PacketCombiner();
        combiner.pack(login, nb);
        nb.flip();
        PacketHeader h = new PacketHeader();
        h.get(nb);
        Serializable pkt = combiner.unpack(h, nb);
        assertTrue(pkt != null);
        LoginRequest login2 = (LoginRequest)pkt;
        assertEquals(0, login2.hash.compareTo(Hash.EMULE));

        Iterator<Tag> itr = login2.properties.iterator();
        assertTrue(itr.hasNext());
        assertEquals(version, itr.next().intValue());
        assertTrue(itr.hasNext());
        assertEquals(capability, itr.next().intValue());
        assertTrue(itr.hasNext());
        assertEquals("jed2k", itr.next().stringValue());
        assertTrue(itr.hasNext());
        assertEquals(versionClient, itr.next().intValue());
        assertFalse(itr.hasNext());
    }

    @Test
    public void testBufferOverflow() throws JED2KException {
        ByteBuffer bb = ByteBuffer.allocate(128);
        LinkedList<Serializable> order = new LinkedList<Serializable>();
        order.add(new IdChange());
        assert(login.bytesCount() < 120);
        order.add(login);
        order.add(login);
        order.add(login);
        order.add(login);
        order.add(login);
        order.add(new IdChange());
        order.add(new Status());
        PacketCombiner combiner = new org.jed2k.protocol.server.PacketCombiner();
        PacketHeader ph = new PacketHeader();
        int flushCount = 0;
        while(!order.isEmpty()) {
            int byteCount = 0;
            Iterator<Serializable> itr = order.iterator();
            while(itr.hasNext()) {
                Serializable pkt = itr.next();
                if (combiner.pack(pkt, bb)) {
                    byteCount += pkt.bytesCount() + ph.bytesCount();
                    itr.remove();
                } else {
                    break;
                }
            }

            assertTrue(byteCount < 128);
            bb.clear();
            ++flushCount;
        }

        assertTrue(flushCount > 1);
    }

    @Test
    public void packetKeyTest() {
        assertTrue(new PacketKey(org.jed2k.protocol.PacketCombiner.ProtocolType.OP_EDONKEYHEADER.value, (byte)0x11)
                .compareTo(new PacketKey(org.jed2k.protocol.PacketCombiner.ProtocolType.OP_EDONKEYHEADER.value, (byte)0x11)) == 0);
        assertTrue(new PacketKey(org.jed2k.protocol.PacketCombiner.ProtocolType.OP_EDONKEYHEADER.value, (byte)0x11)
                .compareTo(new PacketKey(org.jed2k.protocol.PacketCombiner.ProtocolType.OP_EMULEPROT.value, (byte)0x11)) == 1);
        assertTrue(new PacketKey(org.jed2k.protocol.PacketCombiner.ProtocolType.OP_EMULEPROT.value, (byte)0x11)
                .compareTo(new PacketKey(org.jed2k.protocol.PacketCombiner.ProtocolType.OP_EDONKEYHEADER.value, (byte)0x11)) == -1);
        assertTrue(new PacketKey(org.jed2k.protocol.PacketCombiner.ProtocolType.OP_EMULEPROT.value, (byte)0x11)
                .compareTo(new PacketKey(org.jed2k.protocol.PacketCombiner.ProtocolType.OP_EMULEPROT.value, (byte)0x12)) == -1);

        // test packed packet
        assertTrue(new PacketKey(PacketCombiner.ProtocolType.OP_PACKEDPROT.value, (byte)0x11)
                .compareTo(new PacketKey(org.jed2k.protocol.PacketCombiner.ProtocolType.OP_EDONKEYHEADER.value, (byte)0x11)) == 0);
        assertTrue(new PacketKey(PacketCombiner.ProtocolType.OP_EDONKEYHEADER.value, (byte)0x11)
                .compareTo(new PacketKey(org.jed2k.protocol.PacketCombiner.ProtocolType.OP_PACKEDPROT.value, (byte)0x11)) == 0);
        assertTrue(new PacketKey(PacketCombiner.ProtocolType.OP_PACKEDPROT.value, (byte)0x11)
                .compareTo(new PacketKey(org.jed2k.protocol.PacketCombiner.ProtocolType.OP_PACKEDPROT.value, (byte)0x11)) == 0);

        assertTrue(new PacketKey(PacketCombiner.ProtocolType.OP_PACKEDPROT.value, (byte)0x01)
                .compareTo(new PacketKey(org.jed2k.protocol.PacketCombiner.ProtocolType.OP_EDONKEYHEADER.value, (byte)0x10)) == -1);
        assertTrue(new PacketKey(PacketCombiner.ProtocolType.OP_EDONKEYHEADER.value, (byte)0x22)
                .compareTo(new PacketKey(org.jed2k.protocol.PacketCombiner.ProtocolType.OP_PACKEDPROT.value, (byte)0x11)) == 1);
    }
}