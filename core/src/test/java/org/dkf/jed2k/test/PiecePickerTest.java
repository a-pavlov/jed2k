package org.dkf.jed2k.test;


import org.dkf.jed2k.*;
import org.dkf.jed2k.data.PieceBlock;
import org.dkf.jed2k.protocol.NetworkIdentifier;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class PiecePickerTest {

    private final Logger log = LoggerFactory.getLogger(PiecePickerTest.class);
    Peer peer;
    Peer peer2;
    Random rnd;

    @Before
    public void setUp() {
        peer = new Peer(new NetworkIdentifier(100, 3444));
        peer2 = new Peer(new NetworkIdentifier(222, 55456));
        rnd = new Random();
    }

    @Test
    public void testFill() {
        PiecePicker pp = new PiecePicker(2, 10);
        LinkedList<PieceBlock> rq = new LinkedList<PieceBlock>();
        pp.pickPieces(rq, 12, peer, PeerConnection.PeerSpeed.SLOW);
        assertEquals(0, pp.numHave());
        assertEquals(2, pp.numPieces());
        assertEquals(12, rq.size());
        for(int i = 0; i < 10; ++i) {
            assertThat(rq.get(i), is(new PieceBlock(0, i)));
        }

        assertThat(rq.get(10), is(new PieceBlock(0, 10)));
        assertThat(rq.get(11), is(new PieceBlock(0, 11)));
    }

    @Test
    public void testFinishPieces() {
        PiecePicker pp = new PiecePicker(4, 3);
        LinkedList<PieceBlock> rq = new LinkedList<PieceBlock>();
        assertEquals(4, pp.numPieces());
        assertEquals(0, pp.numHave());
        pp.pickPieces(rq, 3, peer, PeerConnection.PeerSpeed.SLOW);
        assertEquals(3, rq.size());
        for(PieceBlock b: rq) {
            pp.markAsFinished(b);
            if (pp.isPieceFinished(b.pieceIndex)) {
                pp.weHave(b.pieceIndex);
            }
        }

        assertEquals(4, pp.numPieces());
        assertEquals(0, pp.numHave());
        rq.clear();
        pp.pickPieces(rq, Constants.BLOCKS_PER_PIECE*3, peer, PeerConnection.PeerSpeed.SLOW);
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
    public void testSmallFile() {
        PiecePicker picker = new PiecePicker(1, 4);
        assertEquals(0, picker.numHave());
        assertEquals(1, picker.totalPieces());
        List<PieceBlock> rq = new LinkedList<>();
        Peer peer = new Peer(new NetworkIdentifier(111, 555));
        picker.pickPieces(rq, Constants.REQUEST_QUEUE_SIZE, peer, PeerConnection.PeerSpeed.SLOW);
        assertEquals(Constants.REQUEST_QUEUE_SIZE, rq.size());
        for(final PieceBlock b: rq) {
            assertTrue(picker.markAsWriting(b));
        }

        for(final PieceBlock b: rq) {
            picker.markAsFinished(b);
        }
        rq.clear();


        picker.pickPieces(rq, Constants.REQUEST_QUEUE_SIZE, peer, PeerConnection.PeerSpeed.SLOW);
        assertEquals(1, rq.size());
        assertEquals(1, picker.numDowloadingPieces());
        assertFalse(picker.isPieceFinished(0));
        assertTrue(picker.markAsWriting(rq.get(0)));
        picker.markAsFinished(rq.get(0));
        picker.weHave(0);
        assertEquals(picker.totalPieces(), picker.numHave());
    }

    @Test
    public void testTrivialFullCycle() {
        PiecePicker pp = new PiecePicker(5, 14);
        LinkedList<PieceBlock> req = new LinkedList<PieceBlock>();
        pp.pickPieces(req, Constants.REQUEST_QUEUE_SIZE, peer, PeerConnection.PeerSpeed.SLOW);
        assertFalse(req.isEmpty());
        int counter = 0;

        while(!req.isEmpty()) {
            for(PieceBlock b: req) {
                assertTrue(pp.markAsFinished(b));
                if (pp.isPieceFinished(b.pieceIndex)) pp.weHave(b.pieceIndex);
                ++counter;
            }

            req.clear();
            pp.pickPieces(req, Constants.REQUEST_QUEUE_SIZE, peer, PeerConnection.PeerSpeed.SLOW);
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
        pp.pickPieces(req, Constants.REQUEST_QUEUE_SIZE, peer, PeerConnection.PeerSpeed.SLOW);
        assertFalse(req.isEmpty());
        int counter = 0;

        while(!req.isEmpty()) {
            for(PieceBlock b: req) {
                if (counter % 5 == 0) pp.abortDownload(b, peer);
                else assertTrue(pp.markAsFinished(b));
                ++counter;
            }

            req.clear();
            pp.pickPieces(req, Constants.REQUEST_QUEUE_SIZE, peer, PeerConnection.PeerSpeed.SLOW);

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
            pp.pickPieces(r, Constants.REQUEST_QUEUE_SIZE, peer, PeerConnection.PeerSpeed.SLOW);
            requestedCount += r.size();
        }

        int requestedEnumerator = 0;
        for(int i = 0; i < 6; ++i) {
            assertFalse(pp.havePiece(i));
            DownloadingPiece dp = pp.getDownloadingPiece(i);
            if (dp != null) {
                Iterator<DownloadingPiece.Block> pbi = dp.iterator();
                while(pbi.hasNext()) {
                    if (pbi.next().isRequested()) requestedEnumerator++;
                }
            }
        }

        assertEquals(requestedCount, requestedEnumerator);
    }

    @Test
    public void testPickOrder() {
        PiecePicker pp = new PiecePicker(3, 2);
        LinkedList<PieceBlock> r = new LinkedList<PieceBlock>();
        pp.pickPieces(r, Constants.REQUEST_QUEUE_SIZE, peer, PeerConnection.PeerSpeed.SLOW);
        assertEquals(Constants.REQUEST_QUEUE_SIZE, r.size());

        LinkedList<PieceBlock> tempReq = new LinkedList<PieceBlock>();
        for(int i = 0; i < 20; i++) {
            tempReq.clear();
            pp.pickPieces(tempReq, Constants.REQUEST_QUEUE_SIZE, peer, PeerConnection.PeerSpeed.SLOW);
            assertEquals(Constants.REQUEST_QUEUE_SIZE, tempReq.size());
        }

        for(final PieceBlock b: r) {
            pp.abortDownload(b, peer);
        }

        for(final PieceBlock b: tempReq) {
            pp.abortDownload(b, peer);
        }

        LinkedList<PieceBlock> newR = new LinkedList<PieceBlock>();
        pp.pickPieces(newR, Constants.REQUEST_QUEUE_SIZE, peer, PeerConnection.PeerSpeed.SLOW);
        assertEquals(Constants.REQUEST_QUEUE_SIZE, newR.size());
        for(int i = 0; i != Constants.REQUEST_QUEUE_SIZE; ++i) {
            assertEquals(r.get(i), newR.get(i));
        }

        newR.clear();
        pp.pickPieces(newR, Constants.REQUEST_QUEUE_SIZE, peer, PeerConnection.PeerSpeed.SLOW);
        assertEquals(Constants.REQUEST_QUEUE_SIZE, newR.size());
        for(int i = 0; i != Constants.REQUEST_QUEUE_SIZE; ++i) {
            assertEquals(tempReq.get(i), newR.get(i));
        }
    }

    @Test
    public void testTrivialEndGame() {
        PiecePicker pp = new PiecePicker(1, 20);
        LinkedList<PieceBlock> rq = new LinkedList<>();
        pp.pickPieces(rq, 19, peer, PeerConnection.PeerSpeed.SLOW);
        assertEquals(19, rq.size());
        LinkedList<PieceBlock> rq2 = new LinkedList<>();
        // end game took first free block and re-request first 3 already requested
        pp.pickPieces(rq2, Constants.REQUEST_QUEUE_SIZE + 1, peer2, PeerConnection.PeerSpeed.MEDIUM);
        assertEquals(4, rq2.size());
        assertEquals(new PieceBlock(0, 19), rq2.get(0));
        for (int i = 0; i < 3; ++i) {
            assertEquals(new PieceBlock(0, i), rq2.get(i+1));
        }

        rq2.clear();

        pp.pickPieces(rq2, Constants.REQUEST_QUEUE_SIZE, peer2, PeerConnection.PeerSpeed.MEDIUM);
        assertEquals(3, rq2.size());
        for (int i = 3; i < 6; ++i) {
            assertEquals(new PieceBlock(0, i), rq2.get(i - 3));
        }

        // slow request returns nothing
        Peer peer3 = new Peer(new NetworkIdentifier(29990, 5678));
        List<PieceBlock> rq3 = new LinkedList<>();
        pp.pickPieces(rq3, Constants.REQUEST_QUEUE_SIZE, peer3, PeerConnection.PeerSpeed.SLOW);
        assertTrue(rq3.isEmpty());

        // fast requested abort download, but blocks still downloading by slow peer1
        for(PieceBlock pb: rq2) {
            pp.abortDownload(pb, peer2);
        }

        pp.pickPieces(rq3, Constants.REQUEST_QUEUE_SIZE, peer3, PeerConnection.PeerSpeed.SLOW);
        assertTrue(rq3.isEmpty());

        // we have one free block after this operation and it will be requested by new slow peer
        pp.abortDownload(new PieceBlock(0, 19), peer2);
        pp.pickPieces(rq3, Constants.REQUEST_QUEUE_SIZE, peer3, PeerConnection.PeerSpeed.SLOW);
        assertEquals(1, rq3.size());
        assertEquals(new PieceBlock(0, 19), rq3.get(0));
    }

    private class PieceManager {
        final PiecePicker picker;
        List<PieceBlock> writeOrder = new LinkedList<>();


        public PieceManager(final PiecePicker picker) {
            this.picker = picker;
        }

        void write(PieceBlock block) {
            writeOrder.add(block);
        }

        private int getFinished() {
            int finishedIndex = -1;
            for (DownloadingPiece dp : picker.getDownloadingQueue()) {
                if (dp.getBlocksCount() == dp.getFinishedBlocksCount()) {
                    finishedIndex = dp.getPieceIndex();
                    break;
                }
            }

            return finishedIndex;
        }

        void process(int index) {
            int failIteration = rnd.nextInt(10);

            int iteration = 0;
            for(final PieceBlock b: writeOrder) {
                if (iteration == failIteration) {
                    log.trace("abort {}", b);
                    picker.abortDownload(b, null);
                } else {
                    log.trace("finish {}", b);
                    picker.markAsFinished(b);
                    int finishedPiece = getFinished();
                    while(finishedPiece != -1) {
                        picker.weHave(finishedPiece);
                        finishedPiece = getFinished();
                    }
                }

                ++iteration;
            }

            writeOrder.clear();
        }
    }

    private class Downloader {
        final PiecePicker picker;
        PeerConnection.PeerSpeed speed;
        private LinkedList<PieceBlock> blocks = new LinkedList<>();
        private final Peer peer = new Peer(new NetworkIdentifier(rnd.nextInt(), rnd.nextInt(30000)));
        private final PieceManager mgr;
        private int counter = 0;

        public Downloader(final PiecePicker picker, PeerConnection.PeerSpeed speed, PieceManager mgr) {
            this.picker = picker;
            this.speed = speed;
            this.mgr = mgr;
        }

        private void doWork() {
            if (blocks.isEmpty()) {
                picker.pickPieces(blocks, Constants.REQUEST_QUEUE_SIZE, peer, speed);
                return;
            }

            PieceBlock block = blocks.poll();
            if (block != null) {
                log.trace("write {}", block);
                if (counter % 5 == 0) picker.abortDownload(block, peer);
                else if (picker.markAsWriting(block)) mgr.write(block);
            }
        }

        void process(int index) {
            if (speed == PeerConnection.PeerSpeed.FAST ||
                    speed == PeerConnection.PeerSpeed.MEDIUM && index % 2 == 0 ||
                    speed == PeerConnection.PeerSpeed.SLOW && index % 3 == 0) {
                ++counter;
                doWork();
            }
        }
    }

    @Test
    public void testFullCycle() {
        PiecePicker picker = new PiecePicker(40, 33);
        List<Downloader> downloaders = new LinkedList<>();
        PieceManager manager = new PieceManager(picker);
        downloaders.add(new Downloader(picker, PeerConnection.PeerSpeed.SLOW, manager));
        downloaders.add(new Downloader(picker, PeerConnection.PeerSpeed.SLOW, manager));
        downloaders.add(new Downloader(picker, PeerConnection.PeerSpeed.SLOW, manager));
        downloaders.add(new Downloader(picker, PeerConnection.PeerSpeed.MEDIUM, manager));
        downloaders.add(new Downloader(picker, PeerConnection.PeerSpeed.MEDIUM, manager));
        downloaders.add(new Downloader(picker, PeerConnection.PeerSpeed.FAST, manager));
        downloaders.add(new Downloader(picker, PeerConnection.PeerSpeed.FAST, manager));
        assertEquals(0, picker.numHave());
        assertEquals(40, picker.totalPieces());

        int iterator = 0;
        while(true) {
            for(final Downloader d: downloaders) {
                d.process(iterator);
            }

            manager.process(iterator);
            ++iterator;

            if (iterator % 100 == 0) log.info(picker.toString());
            if (picker.numHave() == picker.totalPieces()) break;
        }
    }
}
