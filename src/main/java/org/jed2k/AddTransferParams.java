package org.jed2k;

import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.*;
import org.jed2k.protocol.tag.Tag;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 31.07.2016.
 */
public class AddTransferParams implements Serializable {
    public Hash hash;
    public UInt64 size = new UInt64();
    public String filepath;
    public Optional<TransferResumeData> resumeData = new Optional(TransferResumeData.class);

    public AddTransferParams(final Hash h, final long size, final String filepath) {
        hash = h;
        this.size.assign(size);
        this.filepath = filepath;
    }

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {

        return resumeData.get(size.get(hash.get(src)));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        assert(hash != null);
        assert(filepath != null);
        //Tag t = Tag.tag(Tag.FT_FILENAME, "", filepath);
        //assert(t.isStringTag());
        return resumeData.put(size.put(hash.put(dst)));
    }

    @Override
    public int bytesCount() {
        return hash.bytesCount() + size.bytesCount() + resumeData.bytesCount();
    }
}
