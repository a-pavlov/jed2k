package org.dkf.jed2k.protocol.test;

import org.dkf.jed2k.data.PieceBlock;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.*;
import org.dkf.jed2k.protocol.client.Hello;
import org.dkf.jed2k.protocol.kad.*;
import org.dkf.jed2k.protocol.server.*;
import org.dkf.jed2k.protocol.tag.Tag;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.dkf.jed2k.protocol.Unsigned.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by apavlov on 22.05.17.
 */
public class BufferGetRuntimeExceptionTest {

    byte[] data = { (byte)1, (byte)2, (byte)3 };
    ByteBuffer nb = ByteBuffer.wrap(data);

    @Before
    public void setup() {
        nb.order(ByteOrder.LITTLE_ENDIAN);
    }

    @Test(expected = JED2KException.class)
    public void testUint8() throws JED2KException {
        UInt8 u = uint8();
        nb.clear();
        u.get(nb);
        u.get(nb);
        u.get(nb);
        u.get(nb);
    }


    @Test(expected = JED2KException.class)
    public void testUint16() throws JED2KException {
        UInt16 u = uint16();
        nb.clear();
        u.get(nb);
        u.get(nb);
        u.get(nb);
    }

    @Test(expected = JED2KException.class)
    public void testUint32() throws JED2KException {
        UInt32 u = uint32();
        nb.clear();
        u.get(nb);
        u.get(nb);
    }

    @Test(expected = JED2KException.class)
    public void testBitField() throws JED2KException {
        BitField bf = new BitField(128);
        nb.clear();
        bf.get(nb);
    }

    @Test(expected = JED2KException.class)
    public void testByteContainer() throws JED2KException {
        ByteContainer<UInt8> bc = new ByteContainer<UInt8>(uint8(100));
        nb.clear();
        nb.position(2); // have one byte with value 1 as size and no more bytes
        bc.get(nb);
    }

    @Test(expected = JED2KException.class)
    public void testHash() throws JED2KException {
        Hash h = new Hash();
        nb.clear();
        h.get(nb);
    }

    @Test(expected = JED2KException.class)
    public void testOptional() throws JED2KException {
        Optional<UInt32> o = new Optional<>(UInt32.class);
        nb.clear();
        nb.position(3); // no bytes at all
        o.get(nb);
    }

    @Test(expected = JED2KException.class)
    public void testPacketHeader() throws JED2KException {
        PacketHeader ph = new PacketHeader();
        nb.clear();
        ph.get(nb);
    }

    @Test(expected = JED2KException.class)
    public void testPacketHeaderZero() throws JED2KException {
        PacketHeader ph = new PacketHeader();
        nb.clear();
        nb.position(3);
        ph.get(nb);
    }

    @Test(expected = JED2KException.class)
    public void testHello() throws JED2KException {
        Hello h = new Hello();
        nb.clear();
        nb.position(3);
        h.get(nb);
    }


    @Test(expected = JED2KException.class)
    public void testFilewalledUdp() throws JED2KException {
        Kad2FirewalledUdp kad = new Kad2FirewalledUdp();
        nb.clear();
        nb.position(3);
        assertFalse(nb.hasRemaining());
        kad.get(nb);
    }

    @Test(expected = JED2KException.class)
    public void testKad2Req() throws JED2KException {
        Kad2Req req = new Kad2Req();
        nb.clear();
        nb.position(3);
        assertFalse(nb.hasRemaining());
        req.get(nb);
    }

    @Test(expected = JED2KException.class)
    public void testKadId() throws JED2KException {
        KadId id = new KadId();
        nb.clear();
        id.get(nb);
    }

    @Test(expected = JED2KException.class)
    public void testKadPacketHeader() throws JED2KException {
        KadPacketHeader kph = new KadPacketHeader();
        nb.clear();
        nb.position(3);
        assertFalse(nb.hasRemaining());
        kph.get(nb);
    }

    @Test(expected = JED2KException.class)
    public void testServerMet() throws JED2KException {
        ServerMet sm = new ServerMet();
        nb.clear();
        nb.position(3);
        assertFalse(nb.hasRemaining());
        sm.get(nb);
    }

    @Test(expected = JED2KException.class)
    public void testTagInt() throws JED2KException {
        Tag t = Tag.tag(Tag.FT_FILESIZE, "", 1000);
        nb.clear();
        nb.position(3);
        assertFalse(nb.hasRemaining());
        t.get(nb);
    }

    @Test(expected = JED2KException.class)
    public void testTagIntSecondByte() throws JED2KException {
        Tag t = Tag.tag(Tag.FT_FILESIZE, "", 1000);
        nb.clear();
        nb.position(2);
        assertTrue(nb.hasRemaining());
        t.get(nb);
    }

    @Test(expected = JED2KException.class)
    public void testBoolArraySerial() throws JED2KException {
        Tag.BoolArraySerial ba = new Tag.BoolArraySerial();
        nb.clear();
        ba.get(nb);
    }

    @Test(expected = JED2KException.class)
    public void testStringSerial() throws JED2KException {
        Tag.StringSerial ss = new Tag.StringSerial();
        nb.clear();
        nb.position(2);
        ss.get(nb);
    }

    @Test(expected = JED2KException.class)
    public void testBooleanSerial() throws JED2KException {
        Tag.BooleanSerial bs = new Tag.BooleanSerial(false);
        nb.clear();
        nb.position(3);
        assertFalse(nb.hasRemaining());
        bs.get(nb);
    }

    @Test(expected = JED2KException.class)
    public void testFloatSerial() throws JED2KException {
        Tag.FloatSerial fs = new Tag.FloatSerial(120.5f);
        nb.clear();
        fs.get(nb);
    }

    @Test(expected = JED2KException.class)
    public void testPieceBlock() throws JED2KException {
        PieceBlock pb = new PieceBlock(0, 0);
        nb.clear();
        pb.get(nb);
    }

    @Test(expected = JED2KException.class)
    public void testEndpoint() throws JED2KException {
        Endpoint ep = new Endpoint(0, 0);
        nb.clear();
        ep.get(nb);
    }

    @Test(expected = JED2KException.class)
    public void testKad2FirewalledRes() throws JED2KException {
        Kad2FirewalledRes res = new Kad2FirewalledRes();
        nb.clear();
        nb.position(3);
        res.get(nb);
    }

    @Test(expected = JED2KException.class)
    public void testCallbackRequest() throws JED2KException {
        CallbackRequest cr = new CallbackRequest(1);
        nb.clear();
        cr.get(nb);
    }

    @Test(expected = JED2KException.class)
    public void testGetFileSources() throws JED2KException {
        GetFileSources gfs = new GetFileSources();
        nb.clear();
        gfs.get(nb);
    }

    @Test(expected = JED2KException.class)
    public void testIdChange() throws JED2KException{
        IdChange ic = new IdChange();
        nb.clear();
        ic.get(nb);
    }

    @Test(expected = JED2KException.class)
    public void testIdChangeLimit() throws JED2KException{
        IdChange ic = new IdChange();
        nb.clear();
        ic.get(nb, 20);
    }

    @Test(expected = JED2KException.class)
    public void testStatus() throws JED2KException {
        Status s = new Status();
        nb.clear();
        s.get(nb);
    }

}
