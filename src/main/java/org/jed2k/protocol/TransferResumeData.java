package org.jed2k.protocol;

import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.tag.Tag;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 01.07.2016.
 * Transfer resume data for restore transfer state after restart
 *
 */
public class TransferResumeData implements Serializable {
    public Hash hash;
    public UInt64 size = new UInt64();
    public Tag filepath;

    public Container<UInt16, Hash>    hashes  = Container.makeShort(Hash.class);
    public Container<UInt16, PieceResumeData> pieces = Container.makeShort(PieceResumeData.class);
    public Container<UInt16, NetworkIdentifier> peers = Container.makeShort(NetworkIdentifier.class);

    public TransferResumeData() {
        hash = new Hash();
        filepath = new Tag();
    }

    public TransferResumeData(final Hash h, final long size, final String fp) throws JED2KException {
        hash = h;
        this.size.assign(size);
        filepath = Tag.tag(Tag.FT_FILENAME, "", fp);
    }

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return peers.get(pieces.get(hashes.get(filepath.get(size.get(hash.get(src))))));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        assert(filepath != null);
        assert(hash != null);
        assert(filepath.isStringTag());
        return peers.put(pieces.put(hashes.put(filepath.put(size.put(hash.put(dst))))));
    }

    @Override
    public int bytesCount() {
        return  hash.bytesCount() + size.bytesCount() + pieces.bytesCount() + peers.size();
    }
}
