package org.dkf.jed2k;

import org.dkf.jed2k.data.PieceBlock;

import java.util.Collection;
import java.util.Iterator;

public class DownloadingPiece implements Iterable<DownloadingPiece.Block> {

    public enum BlockState {
        STATE_NONE((byte)0),
        STATE_REQUESTED((byte)1),
        STATE_WRITING((byte)2),
        STATE_FINISHED((byte)3);
        byte value;

        BlockState(byte b) {
            value = b;
        }
    }

    public class Block {
        private BlockState   state  = BlockState.STATE_NONE;
        private short downloadersCount = 0;
        private Peer lastDownloader = null;
        private PeerConnection.PeerSpeed speed = PeerConnection.PeerSpeed.SLOW;

        public final boolean isRequested() {    return state == BlockState.STATE_REQUESTED; }
        public final boolean isFinished() { return state == BlockState.STATE_FINISHED; }
        public final boolean isWriting() {  return state == BlockState.STATE_WRITING; }
        public final boolean isFree() { return state == BlockState.STATE_NONE; }

        public void request(Peer p, PeerConnection.PeerSpeed speed) {
            assert state != BlockState.STATE_FINISHED;
            assert state != BlockState.STATE_WRITING;
            ++downloadersCount;
            lastDownloader = p;
            state = BlockState.STATE_REQUESTED;
            this.speed = speed;
        }

        public void write() {
            assert state == BlockState.STATE_REQUESTED || state == BlockState.STATE_NONE;
            downloadersCount = 0;
            lastDownloader = null;
            state = BlockState.STATE_WRITING;
        }

        public void finish() {
            state = BlockState.STATE_FINISHED;
        }

        public void abort(Peer p) {
            assert state != BlockState.STATE_NONE;
            assert state == BlockState.STATE_WRITING || p != null;

            if (downloadersCount > 0) downloadersCount--;
            if (lastDownloader != null && lastDownloader == p) lastDownloader = null;

            if (downloadersCount == 0) {
                state = BlockState.STATE_NONE;
            }
        }

        public int getDownloadersCount() {
            return downloadersCount;
        }

        public Peer getLastDownloader() {
            return lastDownloader;
        }

        public PeerConnection.PeerSpeed getDownloadingSpeed() {
            return speed;
        }

        public BlockState getState() {
            return state;
        }
    }

    public int pieceIndex;
    private int blocksCount;
    public Block[] blocks;

    /*
    TODO - for future usage
    short requestedBlocksCount;
    short writingBlocksCount;
    short finishedBlocksCount;
    short downloadedBlocksCount;
    */

    public DownloadingPiece(int pieceIndex, int blocksCount) {
        assert(pieceIndex >= 0);
        assert(blocksCount > 0);
        this.pieceIndex = pieceIndex;
        this.blocksCount = blocksCount;
        blocks = new Block[blocksCount];
        for(int i = 0; i != blocksCount; ++i) blocks[i] = new Block();
    }

    private int calculateStatedBlocks(BlockState state) {
        int res = 0;
        for(final Block b: blocks) {
            if (b.getState().equals(state)) res++;
        }

        return res;
    }

    public final int getFinishedBlocksCount() {return calculateStatedBlocks(BlockState.STATE_FINISHED); }
    public final int getDownloadingBlocksCount() { return calculateStatedBlocks(BlockState.STATE_REQUESTED); }
    public final int getWritingBlocksCount() { return calculateStatedBlocks(BlockState.STATE_WRITING); }
    public final int downloadedCount() { return calculateStatedBlocks(BlockState.STATE_WRITING) + calculateStatedBlocks(BlockState.STATE_FINISHED); }
    public final int getTotalBlocks() { return blocks.length; }

    public int getBlocksCount() {
        return blocksCount;
    }

    public void finishBlock(int blockIndex) {
        assert(blockIndex < blocksCount);
        assert !blocks[blockIndex].isFinished();
        blocks[blockIndex].finish();
    }

    public void requestBlock(int blockIndex, Peer p, PeerConnection.PeerSpeed speed) {
        assert(blockIndex < blocksCount);
        blocks[blockIndex].request(p, speed);
    }

    public boolean writeBlock(int blockIndex) {
        assert(blockIndex < blocksCount);
        if (blocks[blockIndex].isFree() || blocks[blockIndex].isRequested()) {
            blocks[blockIndex].write();
            return true;
        }

        // block already finished or written, no need write it again
        return false;
    }

    boolean isDownloaded(int blockIndex) {
        assert(blockIndex < blocksCount);
        return blocks[blockIndex].isFinished() || blocks[blockIndex].isWriting();
    }

    boolean isFinished(int blockIndex) {
        assert(blockIndex < blocksCount);
        return blocks[blockIndex].isFinished();
    }

    public void abortDownloading(int blockIndex, Peer p) {
        blocks[blockIndex].abort(p);
    }

    public int pickBlocks(Collection<PieceBlock> rq
            , int orderLength
            , final Peer peer
            , PeerConnection.PeerSpeed speed
            ,  boolean endGame) {

        int res = 0;
        // not end game mode and have no free blocks
        if (!endGame && getDownloadingBlocksCount() == getTotalBlocks()) {
            return res;
        }

        for(int i = 0; i < getTotalBlocks() && res < orderLength; ++i) {
            if (blocks[i].isFree()) {
                rq.add(new PieceBlock(pieceIndex, i));
                blocks[i].request(peer, speed);
                ++res;
                continue;
            }

            if (endGame && blocks[i].isRequested()) {
                if (blocks[i].getDownloadersCount() < 2 && blocks[i].getDownloadingSpeed().compareTo(speed) < 0 && peer != blocks[i].getLastDownloader()) {
                    blocks[i].request(peer, speed);
                    rq.add(new PieceBlock(pieceIndex, i));
                    ++res;
                }
            }
        }

        return res;
    }

    @Override
    public Iterator<DownloadingPiece.Block> iterator() {
        Iterator<DownloadingPiece.Block> it = new Iterator<DownloadingPiece.Block>() {
            private int currentIndex = 0;

            @Override
            public boolean hasNext() {
                assert(blocks != null);
                return currentIndex < blocks.length;
            }

            @Override
            public DownloadingPiece.Block next() {
                return blocks[currentIndex++];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };

        return it;
    }
}
