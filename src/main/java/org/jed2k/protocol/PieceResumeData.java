package org.jed2k.protocol;

import org.jed2k.Utils;
import org.jed2k.exception.JED2KException;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 01.07.2016.
 * Information about one piece state
 *
 */
public class PieceResumeData implements Serializable {

    public enum ResumePieceStatus {
        PARTIAL((byte)0),
        COMPLETED((byte)1),
        NONE((byte)2);

        private byte value;
        ResumePieceStatus(byte val) {
            this.value = val;
        }

        byte getValue() { return value; }
    }

    private ResumePieceStatus status;
    private BitField blocks;

    public PieceResumeData() {
    }

    public PieceResumeData(ResumePieceStatus status, BitField blocks) {
        this.status = status;
        this.blocks = blocks;
        assert(blocks == null || this.status == ResumePieceStatus.PARTIAL);
    }

    public static PieceResumeData makeCompleted() {
        return new PieceResumeData(ResumePieceStatus.COMPLETED, null);
    }

    public static PieceResumeData makeEmpty() {
        return new PieceResumeData(ResumePieceStatus.NONE, null);
    }

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        byte state = src.get();
        if (state == ResumePieceStatus.COMPLETED.getValue()) status = ResumePieceStatus.COMPLETED;
        if (state == ResumePieceStatus.PARTIAL.getValue()) status = ResumePieceStatus.PARTIAL;
        if (state == ResumePieceStatus.NONE.getValue()) status = ResumePieceStatus.NONE;

        if (status == ResumePieceStatus.PARTIAL) {
            assert(blocks == null);
            blocks = new BitField();
            blocks.get(src);
        }

        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        dst.put(status.value);
        if (status == ResumePieceStatus.PARTIAL) {
            assert(blocks != null);
            blocks.put(dst);
        }

        return dst;
    }

    public boolean isPieceCompleted() {
        return status == ResumePieceStatus.COMPLETED;
    }

    public final ResumePieceStatus getPieceStatus() {
        return status;
    }

    public final BitField getBlocksStatus() {
        return blocks;
    }

    @Override
    public int bytesCount() {
        return Utils.sizeof(status.value) + (blocks!=null?blocks.bytesCount():0);
    }
}
