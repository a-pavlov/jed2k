package org.jed2k.protocol.test;

import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.Container;
import org.jed2k.protocol.NetworkIdentifier;
import org.jed2k.protocol.Optional;
import org.jed2k.protocol.UInt8;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Created by inkpot on 31.07.2016.
 */
public class OptionalTest {
    @Test
    public void testOptionalOn() throws JED2KException {
        byte source[] = {
                (byte)0x01, // have data
                (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x05, (byte)0x00};   // net identifier 1

        Optional<NetworkIdentifier> opt = new Optional(NetworkIdentifier.class);
        ByteBuffer nb = ByteBuffer.wrap(source);
        nb.order(ByteOrder.LITTLE_ENDIAN);
        opt.get(nb);
        assertTrue(opt.haveData());

        NetworkIdentifier data = opt.getData();
        assertTrue(data != null);
        assertEquals(new NetworkIdentifier(1, (short)5), data);
    }

    @Test
    public void testOptionalOff() throws JED2KException {
        byte source[] = {
                (byte)0x00, // have no data
                (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x05, (byte)0x00};   // net identifier
        Optional<NetworkIdentifier> opt = new Optional(NetworkIdentifier.class);
        ByteBuffer nb = ByteBuffer.wrap(source);
        nb.order(ByteOrder.LITTLE_ENDIAN);
        opt.get(nb);
        assertFalse(opt.haveData());
        assertTrue(opt.getData() == null);
        assertEquals(6, nb.remaining());
    }

    @Test
    public void testInOut() throws JED2KException {
        ByteBuffer bb = ByteBuffer.allocate(20);
        Optional<NetworkIdentifier> opt1 = new Optional<NetworkIdentifier>(NetworkIdentifier.class);
        Optional<NetworkIdentifier> opt2 = new Optional<NetworkIdentifier>(NetworkIdentifier.class);
        opt2.setData(new NetworkIdentifier(1120, (short)3));
        opt1.put(opt2.put(opt1.put(bb)));
        assertTrue(bb.hasRemaining());
        bb.flip();
        Optional<NetworkIdentifier> opt = new Optional<NetworkIdentifier>(NetworkIdentifier.class);
        opt.get(bb);
        assertFalse(opt.haveData());
        opt.get(bb);
        assertTrue(opt.haveData());
        assertEquals(new NetworkIdentifier(1120, (short)3), opt.getData());
        opt.get(bb);
        assertFalse(opt.haveData());
        assertFalse(bb.hasRemaining());
    }
}
