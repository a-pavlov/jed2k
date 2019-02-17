package org.dkf.jed2k.disk;

import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by inkpot on 30.01.2017.
 */
public class DesktopFileHandler extends FileHandler {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(DesktopFileHandler.class);

    public DesktopFileHandler(final File file) {
        super(file);
    }

    @Override
    protected FileOutputStream allocateOutputStream() throws JED2KException {
        try {
            return new FileOutputStream(file);
        } catch(IOException e) {
            throw new JED2KException(ErrorCode.IO_EXCEPTION);
        }
    }

    @Override
    protected FileInputStream allocateInputStream() throws JED2KException {
        try {
            return new FileInputStream(file);
        } catch(IOException e) {
            throw new JED2KException(ErrorCode.IO_EXCEPTION);
        }
    }

    @Override
    protected void deleteFile() throws JED2KException {
        close();
        if (!file.delete()) throw new JED2KException(ErrorCode.UNABLE_TO_DELETE_FILE);
    }
}
