package org.jed2k.protocol.test;

import org.jed2k.protocol.*;
import org.junit.Test;
import org.jed2k.exception.JED2KException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by inkpot on 01.07.2016.
 */
public class ResumeDataTest {

    @Test
    public void testResumeDataSerialization() throws JED2KException {
        ByteBuffer bb = ByteBuffer.allocate(220);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        TransferResumeData trd = new TransferResumeData(Hash.LIBED2K, 100500L, "xxx");
        trd.hashes.add(Hash.INVALID);
        trd.hashes.add(Hash.EMULE);
        trd.hashes.add(Hash.EMULE);
        trd.hashes.add(Hash.TERMINAL);

        trd.pieces.add(PieceResumeData.makeCompleted());
        trd.pieces.add(PieceResumeData.makeFinished());

        PieceResumeData prd = new PieceResumeData(0, Container.make(new UInt8(0), UInt8.class));
        for(int i = 0; i < 22; ++i) {
            prd.blocks.add(new UInt8(i));
        }
        trd.pieces.add(prd);
        trd.put(bb);
        assertTrue(bb.position() != 0);
        bb.flip();
        TransferResumeData trd2 = new TransferResumeData();
        trd2.get(bb);
        assertEquals(trd2.hash, trd.hash);
        assertEquals(trd2.size, trd.size);
        assertEquals(trd2.hashes.size(), trd.hashes.size());
        Iterator<Hash> itr = trd.hashes.iterator();
        Iterator<Hash> itr2 = trd2.hashes.iterator();
        while(itr.hasNext()) {
            assertEquals(itr.next(), itr2.next());
        }

        assertEquals(trd2.pieces.size(), trd.pieces.size());
        assertEquals(0, trd2.peers.size());
        assertTrue(trd2.filepath.isStringTag());
        assertEquals("xxx", trd2.filepath.stringValue());
    }

}
