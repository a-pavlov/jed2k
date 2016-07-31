package org.jed2k.protocol;

import org.jed2k.Utils;
import org.jed2k.exception.JED2KException;

import java.nio.ByteBuffer;
import java.util.LinkedList;

/**
 * Created by inkpot on 01.07.2016.
 * Information about one piece state
 *
 */
public class PieceResumeData implements Serializable {

    public enum PieceStatus {
        PARTIAL((byte)0),
        COMPLETED((byte)1);

        public byte value;

        PieceStatus(byte val) {
            this.value = val;
        }
    }

    public PieceStatus status = PieceStatus.COMPLETED;
    public Container<UInt8, UInt8>   blocks;

    public PieceResumeData() {
    }

    public PieceResumeData(byte status, Container<UInt8, UInt8> b) {
        this.status.value = status;
        this.blocks = b;
        assert(b != null || this.status != PieceStatus.PARTIAL);
    }

    public static PieceResumeData makeCompleted() {
        return new PieceResumeData((byte)1, null);
    }

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        status.value = src.get();
        if (status == PieceStatus.PARTIAL) {
            blocks = Container.makeByte(UInt8.class);
            blocks.get(src);
        }

        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        dst.put(status.value);
        if (status == PieceStatus.PARTIAL) {
            assert(blocks != null);
            blocks.put(dst);
        }

        return dst;
    }

    @Override
    public int bytesCount() {
        return Utils.sizeof(status.value) + blocks.bytesCount();
    }
}
