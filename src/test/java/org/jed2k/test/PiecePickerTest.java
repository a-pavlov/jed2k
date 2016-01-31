package org.jed2k.test;


import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.Test;
import org.jed2k.Constants;
import org.jed2k.PiecePicker;
import org.jed2k.data.PieceBlock;

public class PiecePickerTest {
    @Test
    public void testFill() {
        PiecePicker pp = new PiecePicker(2, 10);
        for(int i = 0; i < Constants.BLOCKS_PER_PIECE + 10; ++i) {
            int pieceId = i / Constants.BLOCKS_PER_PIECE;
            assertThat(pp.requestBlock(), is((new PieceBlock(pieceId, i - pieceId*Constants.BLOCKS_PER_PIECE))));
        }
    }
    
}