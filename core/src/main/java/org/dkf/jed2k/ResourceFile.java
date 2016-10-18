package org.dkf.jed2k;

import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

/**
 * Created by ap197_000 on 06.09.2016.
 */
public class ResourceFile {

    public ByteBuffer read(final String fileName, ByteBuffer input) throws JED2KException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        FileInputStream stream = null;
        FileChannel channel = null;

        try {
            // do not use try-with-resources due to limit of android api level 16
            stream = new FileInputStream(file);
            channel = stream.getChannel();
            ByteBuffer data = input;

            long fileSize = file.length();

            if (fileSize > Constants.BLOCK_SIZE) throw new JED2KException(ErrorCode.BUFFER_TOO_LARGE);

            if (input == null || input.capacity() < file.length()) {
                data = ByteBuffer.allocate((int)file.length());
            }

            data.order(ByteOrder.LITTLE_ENDIAN);

            while(data.hasRemaining()) channel.read(data);
            data.flip();
            return data;
        }
        catch(IOException e) {
            throw new JED2KException(ErrorCode.FILE_IO_ERROR);
        }
        finally {
            try {
                if (stream != null ) stream.close();
            } catch(IOException e) {
                // just ignore
            }

            try {
                if (channel != null) channel.close();
            } catch(IOException e) {
                // just ignore
            }
        }
    }
}
