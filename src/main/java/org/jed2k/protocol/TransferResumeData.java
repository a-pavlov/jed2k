package org.jed2k.protocol;

import org.jed2k.exception.JED2KException;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 01.07.2016.
 * Transfer resume data for restore transfer state after restart
 *
 */
public class TransferResumeData implements Serializable {
    public Container<UInt16, Hash>    hashes  = Container.makeShort(Hash.class);
    public Container<UInt16, PieceResumeData> pieces = Container.makeShort(PieceResumeData.class);
    public Container<UInt16, NetworkIdentifier> peers = Container.makeShort(NetworkIdentifier.class);

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return peers.get(pieces.get(hashes.get(src)));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return peers.put(pieces.put(hashes.put(dst)));
    }

    public boolean isFinished() {
        if (pieces.isEmpty()) return false;
        for(final PieceResumeData prd: pieces) {
            if (!prd.isPieceCompleted()) return false;
        }

        return true;
    }

    @Override
    public int bytesCount() {
        return hashes.bytesCount() + pieces.bytesCount() + peers.size();
    }
}
