package org.dkf.jed2k.protocol.test;

import org.dkf.jed2k.BlocksEnumerator;
import org.dkf.jed2k.Constants;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by inkpot on 19.07.2016.
 */
public class BlockEnumeratorTest {

    @Test
    public void trivialTest() {
        BlocksEnumerator be = new BlocksEnumerator(1, 1);
        assertEquals(1, be.blocksInPiece(0));
        BlocksEnumerator be2 = new BlocksEnumerator(2, 11);
        assertEquals(Constants.BLOCKS_PER_PIECE, be2.blocksInPiece(0));
        assertEquals(11, be2.blocksInPiece(1));
    }

}
