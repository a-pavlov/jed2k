package org.dkf.jed2k.util;

import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Serializable;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

/**
 * Created by inkpot on 18.12.2016.
 */
public class FUtils {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(FUtils.class);

    public static <T extends Serializable> void read(T data, final File f) throws JED2KException {
        try (RandomAccessFile reader = new RandomAccessFile(f, "r"); FileChannel inChannel = reader.getChannel();) {
            long fileSize = inChannel.size();
            ByteBuffer buffer = ByteBuffer.allocate((int) fileSize);
            inChannel.read(buffer);
            buffer.flip();
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            data.get(buffer);
            log.trace("[fs] read {} bytes", data.bytesCount());
        } catch(IOException e) {
            throw new JED2KException(ErrorCode.IO_EXCEPTION);
        }
    }

    public static <T extends Serializable> void write(T data, final File f) throws JED2KException {
        try (RandomAccessFile reader = new RandomAccessFile(f, "rw");
            FileChannel inChannel = reader.getChannel();) {
            long fileSize = data.bytesCount();
            assert fileSize > 0;
            ByteBuffer buffer = ByteBuffer.allocate((int) fileSize);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            data.put(buffer);
            buffer.flip();
            inChannel.write(buffer);
            log.info("[fs] write {} bytes ", data.bytesCount());
        } catch(IOException e) {
            throw new JED2KException(ErrorCode.IO_EXCEPTION);
        }
    }
}
