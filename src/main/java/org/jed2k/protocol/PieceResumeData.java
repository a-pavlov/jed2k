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

    public enum ResumePieceStatus {
        PARTIAL((byte)0),
        COMPLETED((byte)1),
        NONE((byte)2);

        public byte value;
        ResumePieceStatus(byte val) {
            this.value = val;
        }
    }

    public ResumePieceStatus status = ResumePieceStatus.COMPLETED;
    public BitField blocks;

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
        status.value = src.get();
        if (status.value == ResumePieceStatus.PARTIAL.value) {
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

    @Override
    public int bytesCount() {
        return Utils.sizeof(status.value) + (blocks!=null?blocks.bytesCount():0);
    }
}
