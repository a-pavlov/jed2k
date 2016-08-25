package org.jed2k.protocol.test;

import org.jed2k.AddTransferParams;
import org.jed2k.data.PieceBlock;
import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.Hash;
import org.jed2k.protocol.TransferResumeData;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Created by inkpot on 01.07.2016.
 */
public class ResumeDataTest {

    @Test
    public void testResumeDataSerialization() throws JED2KException {
        TransferResumeData trd = new TransferResumeData();
        trd.hashes.add(Hash.INVALID);
        trd.hashes.add(Hash.EMULE);
        trd.hashes.add(Hash.EMULE);
        trd.hashes.add(Hash.TERMINAL);

        trd.pieces.resize(4);

        trd.pieces.setBit(0);
        trd.pieces.setBit(1);

        for(int i = 0; i < 22; ++i) {
            trd.downloadedBlocks.add(new PieceBlock(2, i));
        }

        ByteBuffer bb = ByteBuffer.allocate(trd.bytesCount());
        bb.order(ByteOrder.LITTLE_ENDIAN);
        trd.put(bb);
        assertFalse(bb.hasRemaining());
        bb.flip();
        TransferResumeData trd2 = new TransferResumeData();
        trd2.get(bb);
        assertFalse(bb.hasRemaining());
        //assertEquals(trd2.hash, trd.hash);
        //assertEquals(trd2.size, trd.size);
        assertEquals(trd2.hashes.size(), trd.hashes.size());
        Iterator<Hash> itr = trd.hashes.iterator();
        Iterator<Hash> itr2 = trd2.hashes.iterator();
        while(itr.hasNext()) {
            assertEquals(itr.next(), itr2.next());
        }

        assertEquals(trd2.pieces.size(), trd.pieces.size());
        assertEquals(0, trd2.peers.size());
        assertTrue(trd2.pieces.getBit(0));
        assertTrue(trd2.pieces.getBit(1));
        assertFalse(trd2.pieces.getBit(2));
        assertFalse(trd2.pieces.getBit(3));
    }

    @Test
    public void testAddTransferParameters() throws JED2KException {
        AddTransferParams atp = new AddTransferParams(Hash.EMULE, 100500L, "xxxx", false);
        ByteBuffer bb = ByteBuffer.allocate(200);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        atp.put(bb);
        bb.flip();
        AddTransferParams atp2 = new AddTransferParams();
        atp2.get(bb);
        assertEquals(atp.hash, atp2.hash);
        assertEquals(atp.filepath.asString(), atp2.filepath.asString());
        assertEquals(atp.paused.intValue(), atp2.paused.intValue());
        assertEquals(atp.size.longValue(), atp2.size.longValue());
    }

}
