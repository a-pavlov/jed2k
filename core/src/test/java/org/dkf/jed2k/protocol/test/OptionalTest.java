package org.dkf.jed2k.protocol.test;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.NetworkIdentifier;
import org.dkf.jed2k.protocol.Optional;
import org.dkf.jed2k.protocol.UInt8;
import org.dkf.jed2k.protocol.Unsigned;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static junit.framework.Assert.*;

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
        bb.order(ByteOrder.LITTLE_ENDIAN);
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

    @Test
    public void testSetter() throws JED2KException {
        ByteBuffer bb = ByteBuffer.allocate(20);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        Optional<NetworkIdentifier> opt = new Optional<>(NetworkIdentifier.class);
        opt.setData(null);
        assertFalse(opt.haveData());
        opt.setData(new NetworkIdentifier(1000, 5000));
        assertTrue(opt.haveData());
        opt.put(bb);
        bb.flip();
        Optional<NetworkIdentifier> opt2 = new Optional<>(NetworkIdentifier.class);
        opt2.get(bb);
        assertEquals(opt, opt2);
    }

    @Test
    public void testEquals() {
        Optional<UInt8> opt = new Optional<>(UInt8.class);
        Optional<UInt8> opt2 = new Optional<>(UInt8.class);
        assertFalse(opt.equals(null));
        assertEquals(opt, opt2);
        opt.setData(Unsigned.uint8());
        assertFalse(opt.equals(opt2));
        assertFalse(opt2.equals(opt));
        opt2.setData(Unsigned.uint8());
        assertEquals(opt, opt2);
        opt.getData().assign(122);
        assertFalse(opt.equals(opt2));
        Optional<NetworkIdentifier> opt3 = new Optional<>(NetworkIdentifier.class);
        assertFalse(opt.equals(opt3));
        opt3.setData(new NetworkIdentifier(23434, 555));
        assertFalse(opt.equals(opt3));
    }
}
