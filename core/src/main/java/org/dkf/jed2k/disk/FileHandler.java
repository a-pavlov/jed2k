package org.dkf.jed2k.disk;

import org.dkf.jed2k.exception.JED2KException;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by inkpot on 30.01.2017.
 * common file interface with open and close(delete) feature
 */
public abstract class FileHandler {
    private static final int WRITE = 0;
    private static final int READ = 1;
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(FileHandler.class);
    protected File file;

    private FileOutputStream wStream;
    private FileInputStream rStream;
    private FileChannel[] channels = {null, null};

    public FileHandler(final File file) {
        this.file = file;
    }

    public FileChannel getWriteChannel() throws JED2KException {
        if (channels[WRITE] == null) {
            assert wStream == null;
            wStream = allocateOutputStream();
            channels[WRITE] = wStream.getChannel();
        }

        return channels[WRITE];
    }

    public FileChannel getReadChannel() throws JED2KException {
        if (channels[READ] == null) {
            assert rStream == null;
            rStream = allocateInputStream();
            channels[READ] = rStream.getChannel();
        }

        return channels[READ];
    }

    public void closeChannels() {
        for(int i = 0; i < channels.length; ++i) {
            try {
                if (channels[i] != null) {
                    channels[i].close();
                }
            } catch(IOException e) {
                log.error("unable to close file channel {}", e.toString());
            }
            finally {
                channels[i] = null;
            }
        }

        if (wStream != null) {
            try {
                wStream.close();
            } catch(IOException e) {
                log.error("unable to close write stream {}", e.toString());
            }
            finally {
                wStream = null;
            }
        }

        if (rStream != null) {
            try {
                rStream.close();
            } catch(IOException e) {
                log.error("unable to close write stream {}", e.toString());
            }
            finally {
                rStream = null;
            }
        }
    }

    public void close() {
        closeChannels();
    }

    public File getFile() {
        return file;
    }


    protected abstract FileOutputStream allocateOutputStream() throws JED2KException;
    protected abstract FileInputStream allocateInputStream() throws JED2KException;
    protected abstract void deleteFile() throws JED2KException;
}
