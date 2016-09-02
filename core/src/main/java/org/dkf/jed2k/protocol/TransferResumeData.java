package org.dkf.jed2k.protocol;

import org.dkf.jed2k.data.PieceBlock;
import org.dkf.jed2k.exception.JED2KException;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 01.07.2016.
 * Transfer resume data for restore transfer state after restart
 *
 */
public class TransferResumeData implements Serializable {
    public Container<UInt16, Hash> hashes  = Container.makeShort(Hash.class);
    public BitField pieces = new BitField();
    public Container<UInt16, PieceBlock> downloadedBlocks = Container.makeShort(PieceBlock.class);
    public Container<UInt16, NetworkIdentifier> peers = Container.makeShort(NetworkIdentifier.class);

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return peers.get(downloadedBlocks.get(pieces.get(hashes.get(src))));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return peers.put(
                downloadedBlocks.put(
                        pieces.put(
                                hashes.put(dst))));
    }

    @Override
    public int bytesCount() {
        return hashes.bytesCount() + pieces.bytesCount() + downloadedBlocks.bytesCount() + peers.bytesCount();
    }
}
