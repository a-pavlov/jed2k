package org.jed2k.protocol.test;

import static junit.framework.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;
import java.util.LinkedList;

import org.jed2k.protocol.UInt16;
import org.junit.Test;
import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.UInt8;
import org.jed2k.protocol.Container;
import org.jed2k.protocol.NetworkIdentifier;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class ContainerHolderTest {
    
    @Test
    public void testContainerSerialization() throws JED2KException {
        byte source[] = {
                (byte)0x02, // size
                (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x05, (byte)0x00,   // net identifier 1
                (byte)0x02, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x06, (byte)0x00};  // net identifier 2

        Container<UInt8, NetworkIdentifier> cni = Container.makeByte(NetworkIdentifier.class);
        ByteBuffer nb = ByteBuffer.wrap(source);
        nb.order(ByteOrder.LITTLE_ENDIAN);
        cni.get(nb);
        assertEquals(2, cni.size());

        Iterator<NetworkIdentifier> itr = cni.iterator();
        assertTrue(itr.hasNext());
        assertEquals(new NetworkIdentifier(1, (short)5), itr.next());
        assertTrue(itr.hasNext());
        assertEquals(new NetworkIdentifier(2, (short)6), itr.next());
        assertFalse(itr.hasNext());
    }

    @Test
    public void testContainerInvariants() {
        Container<UInt16, UInt16> c1 = Container.makeShort(UInt16.class);
        Iterator<UInt16> itr = c1.iterator();
        assertTrue(itr != null);
        assertFalse(itr.hasNext());
        c1.add(new UInt16(10));
        assertTrue(c1.iterator().hasNext());
        assertEquals(10, c1.iterator().next().intValue());
    }
}
