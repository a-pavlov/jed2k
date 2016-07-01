package org.jed2k.protocol;

import org.jed2k.exception.JED2KException;

import java.nio.ByteBuffer;
import java.util.LinkedList;

/**
 * Created by inkpot on 01.07.2016.
 * Transfer resume data for restore stansfer state after restart
 */
public class TransferResumeData implements Serializable {
    public Hash hash = new Hash();
    public UInt64 size = new UInt64();
    public ContainerHolder<UInt16, Hash>    hashes  = ContainerHolder.make16(new LinkedList<Hash>(), Hash.class);
    public ContainerHolder<UInt16, PieceResumeData> pieces = ContainerHolder.make16(new LinkedList<PieceResumeData>(), PieceResumeData.class);

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return pieces.get(hashes.get(size.get(hash.get(src))));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return pieces.put(hashes.put(size.put(hash.put(dst))));
    }

    @Override
    public int bytesCount() {
        return  hash.bytesCount() + size.bytesCount() + pieces.bytesCount();
    }
}
