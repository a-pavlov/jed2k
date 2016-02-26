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
import org.jed2k.protocol.PacketHeader;
import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.PacketCombiner;
import org.jed2k.protocol.Serializable;
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
        assertEquals(version, login2.get(0).intValue());
        assertEquals(capability, login2.get(1).intValue());
        assertEquals("jed2k", login2.get(2).stringValue());
        assertEquals(versionClient, login2.get(3).intValue());
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
}