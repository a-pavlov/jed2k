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
        PARTIAL(0),
        COMPLETED(1),
        FINISHED(2);

        public int value;

        PieceStatus(int val) {
            this.value = val;
        }
    }

    public PieceStatus status = PieceStatus.COMPLETED;
    public ContainerHolder<UInt8, UInt8>   blocks;

    public PieceResumeData() {
    }

    public PieceResumeData(int s, ContainerHolder<UInt8, UInt8> b) {
        this.status.value = s;
        this.blocks = b;
        assert(b != null || this.status != PieceStatus.PARTIAL);
    }

    public static PieceResumeData makeCompleted() {
        return new PieceResumeData(1, null);
    }

    public static PieceResumeData makeFinished() {
        return new PieceResumeData(2, null);
    }

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        status.value = src.getInt();
        if (status == PieceStatus.PARTIAL) {
            blocks = ContainerHolder.make8(new LinkedList<UInt8>(), UInt8.class);
            blocks.get(src);
        }

        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        dst.putInt(status.value);
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
