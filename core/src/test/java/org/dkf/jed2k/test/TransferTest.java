package org.dkf.jed2k.test;

import org.dkf.jed2k.*;
import org.dkf.jed2k.data.PieceBlock;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.Hash;
import org.dkf.jed2k.protocol.TransferResumeData;
import org.junit.Assume;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.ByteBuffer;
import java.util.LinkedList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/**
 * Created by ap197_000 on 25.08.2016.
 */
public class TransferTest {

    @Test
    public void testRestore() throws JED2KException {
        Assume.assumeTrue(!System.getProperty("java.runtime.name").toLowerCase().startsWith("android"));
        Session s = Mockito.mock(Session.class);
        when(s.allocatePoolBuffer()).thenReturn(ByteBuffer.allocate(100));
        TransferResumeData trd = new TransferResumeData();
        trd.hashes.add(Hash.INVALID);
        trd.hashes.add(Hash.EMULE);
        trd.hashes.add(Hash.TERMINAL);
        trd.pieces.resize(3);
        trd.pieces.setBit(0);
        trd.pieces.setBit(2);
        trd.downloadedBlocks.add(new PieceBlock(1, 0));
        trd.downloadedBlocks.add(new PieceBlock(1, 22));
        trd.downloadedBlocks.add(new PieceBlock(1, 33));
        AddTransferParams atp = new AddTransferParams(Hash.EMULE, Time.currentTimeMillis(), Constants.PIECE_SIZE*2 + Constants.BLOCK_SIZE*2 + 334, "", true);
        atp.resumeData.setData(trd);
        Transfer t = Mockito.spy(new Transfer(s, atp));
        doNothing().when(t).asyncRestoreBlock(any(PieceBlock.class), any(ByteBuffer.class));
        assertTrue(t.getPicker().havePiece(0));
        assertTrue(t.getPicker().havePiece(2));
        t.getPicker().markAsFinished(new PieceBlock(1, 0));
        t.getPicker().markAsFinished(new PieceBlock(1, 22));
        t.getPicker().markAsFinished(new PieceBlock(1, 33));
        assertTrue(t.getPicker().isBlockDownloaded(new PieceBlock(1, 0)));
        assertTrue(t.getPicker().isBlockDownloaded(new PieceBlock(1, 22)));
        assertTrue(t.getPicker().isBlockDownloaded(new PieceBlock(1, 33)));
    }

    @Test
    public void testBytesDonePartialBlock() throws JED2KException {
        long fileSize = Constants.PIECE_SIZE*3 + Constants.BLOCK_SIZE*2 + 334;  // 4 pieces and 3 blocks in last piece
        PiecePicker picker = new PiecePicker(4, 3);
        Transfer t = new Transfer(new AddTransferParams(Hash.EMULE, Time.currentTimeMillis(), fileSize, "", true), picker);
        picker.restoreHave(0);
        picker.restoreHave(1);

        Peer peer = new Peer(new Endpoint(0, 0));
        // move blocks to downloading queue
        LinkedList<PieceBlock> pieces = new LinkedList<>();
        picker.pickPieces(pieces, Constants.BLOCKS_PER_PIECE + 3, peer, PeerConnection.PeerSpeed.SLOW);
        assertEquals(Constants.BLOCKS_PER_PIECE + 3, pieces.size());

        // piece 2 four blocks are downloaded
        picker.markAsDownloading(new PieceBlock(2, 0), peer);
        picker.markAsDownloading(new PieceBlock(2, 1), peer);
        picker.markAsDownloading(new PieceBlock(2, 2), peer);
        picker.markAsFinished(new PieceBlock(2, 3));
        picker.markAsFinished(new PieceBlock(2, 4));
        picker.markAsWriting(new PieceBlock(2, 5));
        picker.markAsDownloading(new PieceBlock(2, 40), peer);
        picker.markAsDownloading(new PieceBlock(2, 34), peer);
        picker.markAsWriting(new PieceBlock(2, 49));

        // piece 3 two blocks are downloaded
        picker.markAsDownloading(new PieceBlock(3, 0), peer);
        picker.markAsWriting(new PieceBlock(3, 1));
        picker.markAsFinished(new PieceBlock(3, 2));

        TransferStatus status = new TransferStatus();
        t.getBytesDone(status);
        assertEquals(Constants.PIECE_SIZE*2 + Constants.BLOCK_SIZE*4 + Constants.BLOCK_SIZE + 334, status.totalDone);
    }

    @Test
    public void testBytesDonePartialPiece() throws JED2KException {
        long fileSize = Constants.PIECE_SIZE - 1024;
        PiecePicker picker = new PiecePicker(1, Constants.BLOCKS_PER_PIECE - 1);
        Transfer t = new Transfer(new AddTransferParams(Hash.EMULE, Time.currentTimeMillis(), fileSize, "", true), picker);
        picker.restoreHave(0);
        TransferStatus status = new TransferStatus();
        t.getBytesDone(status);
        assertEquals(Constants.PIECE_SIZE - 1024, status.totalDone);
    }

    @Test
    public void testBytesDoneSparse() throws JED2KException {
        long fileSize = Constants.PIECE_SIZE*2 - 1024;
        PiecePicker picker = new PiecePicker(2, Constants.BLOCKS_PER_PIECE);
        Transfer t = new Transfer(new AddTransferParams(Hash.EMULE, Time.currentTimeMillis(), fileSize, "", true), picker);

        // we have 1 piece(last) without 1024 bytes + 2 blocks in first piece
        Peer peer = new Peer(new Endpoint(0, 0));
        picker.restoreHave(1);
        LinkedList<PieceBlock> pieces = new LinkedList<>();
        picker.pickPieces(pieces, 3, peer, PeerConnection.PeerSpeed.SLOW);
        picker.markAsDownloading(new PieceBlock(0, 0), peer);
        picker.markAsFinished(new PieceBlock(0, 49));
        picker.markAsWriting(new PieceBlock(0, 48));

        TransferStatus status = new TransferStatus();
        t.getBytesDone(status);
        assertEquals(Constants.PIECE_SIZE - 1024 + Constants.BLOCK_SIZE*2, status.totalDone);
    }
}
