package org.dkf.jed2k.protocol.test;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Container;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.UInt16;
import org.dkf.jed2k.protocol.UInt8;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;

import static junit.framework.Assert.*;

public class ContainerHolderTest {

    @Test
    public void testContainerSerialization() throws JED2KException {
        byte source[] = {
                (byte)0x02, // size
                (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x05, (byte)0x00,   // net identifier 1
                (byte)0x02, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x06, (byte)0x00};  // net identifier 2

        Container<UInt8, Endpoint> cni = Container.makeByte(Endpoint.class);
        ByteBuffer nb = ByteBuffer.wrap(source);
        nb.order(ByteOrder.LITTLE_ENDIAN);
        cni.get(nb);
        assertEquals(2, cni.size());

        Iterator<Endpoint> itr = cni.iterator();
        assertTrue(itr.hasNext());
        assertEquals(new Endpoint(1, (short)5), itr.next());
        assertTrue(itr.hasNext());
        assertEquals(new Endpoint(2, (short)6), itr.next());
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

    @Test
    public void testContainerRemoveItems() {
        Container<UInt16, Endpoint> c = Container.makeShort(Endpoint.class);
        c.remove(null);
        assertTrue(c.isEmpty());
        c.add(new Endpoint(10, 20));
        c.add(new Endpoint(30, 50));
        assertEquals(2, c.size());
        c.remove(new Endpoint(10, 20));
        assertEquals(1, c.size());
        assertEquals(c.get(0), new Endpoint(30, 50));
    }
}
