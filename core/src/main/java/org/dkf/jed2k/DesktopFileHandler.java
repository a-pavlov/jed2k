package org.dkf.jed2k;

import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

/**
 * Created by inkpot on 30.01.2017.
 */
@Slf4j
public class DesktopFileHandler implements FileHandler {
    private File file;
    private FileChannel rChannel = null;
    private FileChannel wChannel = null;
    private FileInputStream rStream = null;
    private FileOutputStream wStream = null;

    public DesktopFileHandler(final File file) {
        this.file = file;
    }

    private void releaseResources() {
        try {
            if (rChannel != null) {
                rChannel.close();
            }

            if (rStream != null) {
                rStream.close();
            }
        } catch(Exception e) {
            log.error("[desktop file handler] release resources error {}", e);
        } finally {
            rStream = null;
            rChannel = null;
        }

        try {
            if (wChannel != null) {
                wChannel.close();
            }

            if (wStream != null) {
                wStream.close();
            }
        } catch(Exception e) {
            log.error("[desktop file handler] release resources error {}", e);
        } finally {
            wStream = null;
            wChannel = null;
        }
    }

    @Override
    public FileChannel getWriteChannel() throws JED2KException {
        if (wChannel != null) return wChannel;
        try {
            wStream = new FileOutputStream(file);
            wChannel = wStream.getChannel();
            return wChannel;
        }
        catch(FileNotFoundException e) {
            log.error("[desktop file handler] file not found {} on {}"
                    , file
                    , e);
            releaseResources();
            throw new JED2KException(ErrorCode.FILE_NOT_FOUND);
        }
        catch(SecurityException e) {
            log.error("[desktop file handler] security exception {} on {}"
                    , file
                    , e);
            releaseResources();
            throw new JED2KException(ErrorCode.SECURITY_EXCEPTION);
        }
    }

    @Override
    public FileChannel getReadChannel() throws JED2KException {
        if (rChannel != null) return rChannel;
        try {
            rStream = new FileInputStream(file);
            rChannel = rStream.getChannel();
            return rChannel;
        }
        catch(FileNotFoundException e) {
            log.error("[desktop file handler] file not found {} on {}"
                    , file
                    , e);
            releaseResources();
            throw new JED2KException(ErrorCode.FILE_NOT_FOUND);
        }
        catch(SecurityException e) {
            log.error("[desktop file handler] security exception {} on {}"
                    , file
                    , e);
            releaseResources();
            throw new JED2KException(ErrorCode.SECURITY_EXCEPTION);
        }
    }

    @Override
    public void close() throws JED2KException {
        releaseResources();
    }

    @Override
    public void delete() throws JED2KException {
        releaseResources();
        if (!file.delete()) {
            throw new JED2KException(ErrorCode.UNABLE_TO_DELETE_FILE);
        }
    }

    @Override
    public File getFile() {
        return file;
    }
}
