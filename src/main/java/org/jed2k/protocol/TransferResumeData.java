package org.jed2k.protocol;

import org.jed2k.exception.JED2KException;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 01.07.2016.
 * Transfer resume data for restore stansfer state after restart
 */
public class TransferResumeData implements Serializable {
    public Hash hash = new Hash();
    public UInt64 size = new UInt64();
    public Container<UInt16, Hash>    hashes  = Container.makeShort(Hash.class);
    public Container<UInt16, PieceResumeData> pieces = Container.makeShort(PieceResumeData.class);
    public Container<UInt16, NetworkIdentifier> peers = Container.makeShort(NetworkIdentifier.class);

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return peers.get(pieces.get(hashes.get(size.get(hash.get(src)))));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return peers.put(pieces.put(hashes.put(size.put(hash.put(dst)))));
    }

    @Override
    public int bytesCount() {
        return  hash.bytesCount() + size.bytesCount() + pieces.bytesCount() + peers.size();
    }
}
