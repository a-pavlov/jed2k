package org.jed2k;

import org.jed2k.data.PieceBlock;
import org.jed2k.exception.ErrorCode;
import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.Hash;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by inkpot on 15.07.2016.
 * executes write data into file on disk
 */
public class PieceManager extends BlocksEnumerator {
    private String filepath;
    private RandomAccessFile file;
    private FileChannel channel;
    private LinkedList<BlockManager> blockMgrs = new LinkedList<BlockManager>();
    private Logger log = LoggerFactory.getLogger(PieceManager.class);

    public PieceManager(String filepath, int pieceCount, int blocksInLastPiece) {
        super(pieceCount, blocksInLastPiece);
        this.filepath = filepath;
    }

    /**
     * create or open file on disk
     */
    private void open() throws JED2KException {
        if (file == null) {
            try {
                file = new RandomAccessFile(filepath, "rw");
                channel = file.getChannel();
            }
            catch(FileNotFoundException e) {
                throw new JED2KException(ErrorCode.FILE_NOT_FOUND);
            }
            catch(SecurityException e) {
                throw new JED2KException(ErrorCode.SECURITY_EXCEPTION);
            }
        }
    }

    private BlockManager getBlockManager(int piece) {
        for(BlockManager mgr: blockMgrs) {
            if (mgr.getPieceIndex() == piece) return mgr;
        }

        blockMgrs.addLast(new BlockManager(piece, blocksInPiece(piece)));
        return blockMgrs.getLast();
    }

    /**
     * actual write data to file
     * @param b block
     * @param buffer data source
     */
    public LinkedList<ByteBuffer> writeBlock(PieceBlock b, final ByteBuffer buffer) throws JED2KException {
        open();
        assert(file != null);
        assert(channel != null);
        long bytesOffset = b.blocksOffset()*Constants.BLOCK_SIZE;
        BlockManager mgr = getBlockManager(b.pieceIndex);
        assert(mgr != null);

        // TODO - add error handling here with correct buffer return to requester
        try {
            // stage 1 - write block to disk, possibly error occurred
            // buffer must have remaining data
            assert(buffer.hasRemaining());
            log.debug("write buffer remaining {} to offset {}", buffer.remaining(), bytesOffset);
            channel.position(bytesOffset);
            while(buffer.hasRemaining()) channel.write(buffer);
            buffer.rewind();
        }
        catch(IOException e) {
            throw new JED2KException(ErrorCode.IO_EXCEPTION);
        }

        // stage 2 - prepare hash and return obsolete blocks if possible
        LinkedList<ByteBuffer> res = mgr.registerBlock(b.pieceBlock, buffer);
        return res;
    }

    public Hash hashPiece(int pieceIndex) {
        BlockManager mgr = getBlockManager(pieceIndex);
        assert(mgr != null);
        blockMgrs.remove(mgr);
        return mgr.pieceHash();
    }
}
