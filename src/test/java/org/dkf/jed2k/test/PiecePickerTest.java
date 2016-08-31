package org.dkf.jed2k.test;


import org.dkf.jed2k.Constants;
import org.dkf.jed2k.DownloadingPiece;
import org.dkf.jed2k.PiecePicker;
import org.dkf.jed2k.data.PieceBlock;
import org.junit.Test;

import java.util.Iterator;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

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
            if (pp.isPieceFinished(b.pieceIndex)) {
                pp.weHave(b.pieceIndex);
            }
        }

        assertEquals(4, pp.numPieces());
        assertEquals(1, pp.numHave());
        rq.clear();
        pp.pickPieces(rq, Constants.BLOCKS_PER_PIECE*3);
        assertEquals(Constants.BLOCKS_PER_PIECE*3, rq.size());
        for(PieceBlock b: rq) {
            pp.markAsFinished(b);
            if (pp.isPieceFinished(b.pieceIndex)) {
                pp.weHave(b.pieceIndex);
            }
        }

        assertEquals(4, pp.numPieces());
        assertEquals(4, pp.numHave());
    }

    @Test
    public void testResumeDataLoad() {
        PiecePicker pp = new PiecePicker(3, 22);
        pp.restoreHave(0);
        assertEquals(1, pp.numHave());
        assertEquals(0, pp.numDowloadingPieces());
        pp.weHaveBlock(new PieceBlock(1, 0));
        pp.weHaveBlock(new PieceBlock(1, 1));
        pp.weHaveBlock(new PieceBlock(1, 44));
        assertEquals(1, pp.numHave());
        assertEquals(1, pp.numDowloadingPieces());
    }

    @Test
    public void testTrivialFullCycle() {
        PiecePicker pp = new PiecePicker(5, 14);
        LinkedList<PieceBlock> req = new LinkedList<PieceBlock>();
        pp.pickPieces(req, Constants.REQUEST_QUEUE_SIZE);
        assertFalse(req.isEmpty());
        int counter = 0;

        while(!req.isEmpty()) {
            for(PieceBlock b: req) {
                assertTrue(pp.markAsFinished(b));
                if (pp.isPieceFinished(b.pieceIndex)) pp.weHave(b.pieceIndex);
                ++counter;
            }

            req.clear();
            pp.pickPieces(req, Constants.REQUEST_QUEUE_SIZE);
        }

        assertEquals(0, pp.numDowloadingPieces());
        assertEquals(pp.numPieces(), pp.numHave());

        int approximateCounter = Constants.BLOCKS_PER_PIECE*4 + 14;
        assertEquals(approximateCounter, counter);
    }

    @Test
    public void testFullCycleWithAbort() {
        PiecePicker pp = new PiecePicker(6, 14);
        LinkedList<PieceBlock> req = new LinkedList<PieceBlock>();
        pp.pickPieces(req, Constants.REQUEST_QUEUE_SIZE);
        assertFalse(req.isEmpty());
        int counter = 0;

        while(!req.isEmpty()) {
            for(PieceBlock b: req) {
                if (counter % 5 == 0) pp.abortDownload(b);
                else assertTrue(pp.markAsFinished(b));
                ++counter;
            }

            req.clear();
            pp.pickPieces(req, Constants.REQUEST_QUEUE_SIZE);

            for(int i = 0; i < 6; ++i) {
                if (pp.isPieceFinished(i) && !pp.havePiece(i)) {
                    pp.weHave(i);
                }
            }
        }

        assertEquals(0, pp.numDowloadingPieces());

        int approximateCounter = Constants.BLOCKS_PER_PIECE*5 + 14 + (Constants.BLOCKS_PER_PIECE*5 + 14) / 5;
        assertTrue(counter > approximateCounter);
    }

    @Test
    public void testIteration() {
        PiecePicker pp = new PiecePicker(6, 14);

        int requestedCount = 0;
        while(requestedCount < Constants.BLOCKS_PER_PIECE*2 + 12) {
            LinkedList<PieceBlock> r = new LinkedList<PieceBlock>();
            pp.pickPieces(r, Constants.REQUEST_QUEUE_SIZE);
            requestedCount += r.size();
        }

        int requestedEnumerator = 0;
        for(int i = 0; i < 6; ++i) {
            assertFalse(pp.havePiece(i));
            DownloadingPiece dp = pp.getDownloadingPiece(i);
            if (dp != null) {
                Iterator<DownloadingPiece.BlockState> pbi = dp.iterator();
                while(pbi.hasNext()) {
                    if (pbi.next() == DownloadingPiece.BlockState.STATE_REQUESTED) requestedEnumerator++;
                }
            }
        }

        assertEquals(requestedCount, requestedEnumerator);
    }

    @Test
    public void testPickOrder() {
        PiecePicker pp = new PiecePicker(3, 2);
        LinkedList<PieceBlock> r = new LinkedList<PieceBlock>();
        pp.pickPieces(r, Constants.REQUEST_QUEUE_SIZE);
        assertEquals(Constants.REQUEST_QUEUE_SIZE, r.size());

        LinkedList<PieceBlock> tempReq = new LinkedList<PieceBlock>();
        for(int i = 0; i < 20; i++) {
            tempReq.clear();
            pp.pickPieces(tempReq, Constants.REQUEST_QUEUE_SIZE);
            assertEquals(Constants.REQUEST_QUEUE_SIZE, tempReq.size());
        }

        for(final PieceBlock b: r) {
            pp.abortDownload(b);
        }

        for(final PieceBlock b: tempReq) {
            pp.abortDownload(b);
        }

        LinkedList<PieceBlock> newR = new LinkedList<PieceBlock>();
        pp.pickPieces(newR, Constants.REQUEST_QUEUE_SIZE);
        assertEquals(Constants.REQUEST_QUEUE_SIZE, newR.size());
        for(int i = 0; i != Constants.REQUEST_QUEUE_SIZE; ++i) {
            assertEquals(r.get(i), newR.get(i));
        }

        newR.clear();
        pp.pickPieces(newR, Constants.REQUEST_QUEUE_SIZE);
        assertEquals(Constants.REQUEST_QUEUE_SIZE, newR.size());
        for(int i = 0; i != Constants.REQUEST_QUEUE_SIZE; ++i) {
            assertEquals(tempReq.get(i), newR.get(i));
        }
    }
}
