package org.jed2k;

import org.jed2k.data.PieceBlock;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;

/**
 * Created by inkpot on 15.07.2016.
 * executes write data into file on disk
 */
public class PieceManager {
    private String filepath;
    private RandomAccessFile file;
    private FileChannel channel;
    private LinkedList<BlockManager> blockMgrs = new LinkedList<BlockManager>();

    public PieceManager(String filepath) {
        this.filepath = filepath;
    }

    /**
     * create or open file on disk
     */
    private void open() {
        if (file == null) {
            try {
                file = new RandomAccessFile(filepath, "rw");
                channel = file.getChannel();
            }
            catch(FileNotFoundException e) {

            }
            catch(SecurityException e) {

            }
        }
    }

    private BlockManager getBlockManager(int piece) {
        for(BlockManager mgr: blockMgrs) {
            if (mgr.getPieceIndex() == piece) return mgr;
        }

        blockMgrs.addLast(new BlockManager(piece, Constants.BLOCKS_PER_PIECE));
        return blockMgrs.getLast();
    }

    /**
     * actual write data to file
     * @param b block
     * @param buffer data source
     */
    public void writeBlock(PieceBlock b, ByteBuffer buffer) {
        open();
        assert(file != null);
        assert(channel != null);
        long bytesOffset = b.blocksOffset()*Constants.BLOCK_SIZE;
        try {
            BlockManager mgr = getBlockManager(b.piece_index);
            assert(mgr != null);
            mgr.registerBlock(b.piece_block, buffer);
            buffer.rewind();
            channel.position(bytesOffset);
            while(buffer.hasRemaining()) channel.write(buffer);
            // generate result here
        }
        catch(IOException e) {

        }
    }
}
