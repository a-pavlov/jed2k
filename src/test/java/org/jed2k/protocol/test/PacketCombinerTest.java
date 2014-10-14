package org.jed2k.protocol.test;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertEquals;
import static org.jed2k.protocol.tag.Tag.tag;

import java.nio.ByteBuffer;

import org.junit.Test;
import org.jed2k.protocol.Hash;
import org.jed2k.protocol.LoginRequest;
import org.jed2k.protocol.NetworkBuffer;
import org.jed2k.protocol.NetworkIdentifier;
import org.jed2k.protocol.ProtocolException;
import org.jed2k.protocol.PacketCombiner;
import org.jed2k.protocol.Serializable;
import org.jed2k.protocol.tag.Tag;

public class PacketCombinerTest {
    @Test
    public void testPackUnpack() throws ProtocolException {
        ByteBuffer bb = ByteBuffer.allocate(1024);
        NetworkBuffer nb = new NetworkBuffer(bb);
        LoginRequest login = new LoginRequest();
        login.hash = Hash.EMULE;        
        int version = 10;
        int capability = 20;
        int versionClient = 109;
        login.properties.add(tag(Tag.CT_VERSION, null, version));
        login.properties.add(tag(Tag.CT_SERVER_FLAGS, null, capability));
        login.properties.add(tag(Tag.CT_NAME, null, "jed2k"));
        login.properties.add(tag(Tag.CT_EMULE_VERSION, null, versionClient));
        PacketCombiner combiner = new PacketCombiner();
        combiner.pack(login, nb);
        bb.flip();
        Serializable pkt = combiner.unpack(nb);
        assertTrue(pkt != null);
        LoginRequest login2 = (LoginRequest)pkt;
        assertEquals(0, login2.hash.compareTo(Hash.EMULE));
        assertEquals(version, login2.get(0).intValue());
        assertEquals(capability, login2.get(1).intValue());
        assertEquals("jed2k", login2.get(2).stringValue());
        assertEquals(versionClient, login2.get(3).intValue());
    }
}