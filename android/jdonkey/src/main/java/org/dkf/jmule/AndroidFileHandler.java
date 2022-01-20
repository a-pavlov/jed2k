package org.dkf.jmule;

import android.os.ParcelFileDescriptor;

import androidx.documentfile.provider.DocumentFile;

import org.dkf.jed2k.disk.FileHandler;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by inkpot on 31.01.2017.
 */
public class AndroidFileHandler extends FileHandler {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(AndroidFileHandler.class);
    private final DocumentFile doc;
    private final ParcelFileDescriptor descriptor;

    public AndroidFileHandler(final File file, final DocumentFile doc, final ParcelFileDescriptor descriptor) {
        super(file);
        this.doc = doc;
        this.descriptor = descriptor;
    }

    @Override
    protected FileOutputStream allocateOutputStream() throws JED2KException {
        return new FileOutputStream(descriptor.getFileDescriptor());
    }

    @Override
    protected FileInputStream allocateInputStream() throws JED2KException {
        return new FileInputStream(descriptor.getFileDescriptor());
    }

    @Override
    protected void deleteFile() throws JED2KException {
        if (!doc.delete()) {
            throw new JED2KException(ErrorCode.UNABLE_TO_DELETE_FILE);
        }
    }

    @Override
    public void close() {
        super.close();
        try {
            descriptor.close();
        } catch(IOException e) {
            log.error("unable to close file descriptor {}", e.toString());
        }
    }
}
