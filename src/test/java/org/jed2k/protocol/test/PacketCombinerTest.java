package org.jed2k.protocol.test;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertEquals;
import static org.jed2k.protocol.tag.Tag.tag;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;
import java.util.LinkedList;

import org.junit.Test;
import org.jed2k.protocol.Hash;
import org.jed2k.protocol.LoginRequest;
import org.jed2k.protocol.PacketHeader;
import org.jed2k.protocol.ProtocolException;
import org.jed2k.protocol.PacketCombiner;
import org.jed2k.protocol.Serializable;
import org.jed2k.protocol.ServerIdChange;
import org.jed2k.protocol.ServerStatus;
import org.jed2k.protocol.tag.Tag;

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
        } catch(ProtocolException e) {
            
        }
        login.properties.add(tag(Tag.CT_EMULE_VERSION, null, versionClient));
    }
    
    @Test
    public void testPackUnpack() throws ProtocolException {
        ByteBuffer nb = ByteBuffer.allocate(1024);
        nb.order(ByteOrder.LITTLE_ENDIAN);        
        PacketCombiner combiner = new PacketCombiner();
        combiner.pack(login, nb);
        nb.flip();
        Serializable pkt = combiner.unpack(nb);
        assertTrue(pkt != null);
        LoginRequest login2 = (LoginRequest)pkt;
        assertEquals(0, login2.hash.compareTo(Hash.EMULE));
        assertEquals(version, login2.get(0).intValue());
        assertEquals(capability, login2.get(1).intValue());
        assertEquals("jed2k", login2.get(2).stringValue());
        assertEquals(versionClient, login2.get(3).intValue());
    }
    
    @Test
    public void testBufferOverflow() throws ProtocolException {
        ByteBuffer bb = ByteBuffer.allocate(128);
        LinkedList<Serializable> order = new LinkedList<Serializable>();
        order.add(new ServerIdChange());
        assert(login.size() < 120);
        order.add(login);
        order.add(login);
        order.add(login);
        order.add(login);
        order.add(login);
        order.add(new ServerIdChange());
        order.add(new ServerStatus());
        PacketCombiner combiner = new PacketCombiner();        
        PacketHeader ph = new PacketHeader();
        int flushCount = 0;
        while(!order.isEmpty()) {
            int byteCount = 0;
            Iterator<Serializable> itr = order.iterator();
            while(itr.hasNext()) {
                Serializable pkt = itr.next();                
                if (combiner.pack(pkt, bb)) {
                    byteCount += pkt.size() + ph.size();
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
}