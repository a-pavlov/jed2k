package org.dkf.jed2k;

import lombok.Getter;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.*;

import java.io.File;
import java.nio.ByteBuffer;

/**
 * Created by inkpot on 31.07.2016.
 */
@Getter
public class AddTransferParams implements Serializable {
    private final Hash hash = new Hash();
    private final UInt64 createTime = new UInt64();
    private final UInt64 size = new UInt64();
    private final ByteContainer<UInt16> filepath = new ByteContainer<UInt16>(Unsigned.uint16());
    private final UInt8 paused = Unsigned.uint8();
    public Optional<TransferResumeData> resumeData = new Optional(TransferResumeData.class);
    private FileHandler handler;

    public AddTransferParams() {
    }

    public AddTransferParams(final Hash h, long createTime, long size, final File file, final boolean paused) throws JED2KException {
        hash.assign(h);
        this.size.assign(size);
        this.createTime.assign(createTime);
        this.filepath.assignString(file.getAbsolutePath());
        this.paused.assign(paused?1:0);
        handler = new DesktopFileHandler(file);
    }

    public AddTransferParams(final Hash h, long createTime, long size, final FileHandler handler, final boolean paused) throws JED2KException {
        hash.assign(h);
        this.size.assign(size);
        this.createTime.assign(createTime);
        this.filepath.assignString(handler.getFile().getAbsolutePath());
        this.paused.assign(paused?1:0);
        this.handler = handler;
    }

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return resumeData.get(paused.get(filepath.get(size.get(createTime.get(hash.get(src))))));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        assert(hash != null);
        assert(filepath != null);
        return resumeData.put(paused.put(filepath.put(size.put(createTime.put(hash.put(dst))))));
    }

    @Override
    public int bytesCount() {
        return hash.bytesCount() + createTime.bytesCount() + size.bytesCount() + filepath.bytesCount() + paused.bytesCount() + resumeData.bytesCount();
    }
}
