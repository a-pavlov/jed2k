package org.jed2k.test;


import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.jed2k.DownloadingPiece;
import org.junit.Test;
import org.jed2k.Constants;
import org.jed2k.PiecePicker;
import org.jed2k.data.PieceBlock;

import java.util.LinkedList;

public class PiecePickerTest {

    @Test
    public void testFill() {
        PiecePicker pp = new PiecePicker(2, 10);
        LinkedList<PieceBlock> rq = new LinkedList<PieceBlock>();
        pp.pickPieces(rq, 12);
        assertEquals(0, pp.numHave());
        assertEquals(2, pp.numPieces());
        assertEquals(12, rq.size());
        for(int i = 0; i < 10; ++i) {
            assertThat(rq.get(i), is(new PieceBlock(1, i)));
        }

        assertThat(rq.get(10), is(new PieceBlock(0, 0)));
        assertThat(rq.get(11), is(new PieceBlock(0, 1)));
    }

    @Test
    public void testFinishPieces() {
        PiecePicker pp = new PiecePicker(4, 3);
        LinkedList<PieceBlock> rq = new LinkedList<PieceBlock>();
        assertEquals(4, pp.numPieces());
        assertEquals(0, pp.numHave());
        pp.pickPieces(rq, 3);
        assertEquals(3, rq.size());
        for(PieceBlock b: rq) {
            pp.markAsFinished(b);
        }

        assertEquals(4, pp.numPieces());
        assertEquals(1, pp.numHave());
        rq.clear();
        pp.pickPieces(rq, Constants.BLOCKS_PER_PIECE*3);
        assertEquals(Constants.BLOCKS_PER_PIECE*3, rq.size());
        for(PieceBlock b: rq) {
            pp.markAsFinished(b);
        }

        assertEquals(4, pp.numPieces());
        assertEquals(4, pp.numHave());
    }
}