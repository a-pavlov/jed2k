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
        assertEquals(12, rq.size());
        for(int i = 0; i < 10; ++i) {
            assertThat(rq.get(i), is(new PieceBlock(1, i)));
        }

        assertThat(rq.get(10), is(new PieceBlock(0, 0)));
        assertThat(rq.get(11), is(new PieceBlock(0, 1)));
    }
    
}