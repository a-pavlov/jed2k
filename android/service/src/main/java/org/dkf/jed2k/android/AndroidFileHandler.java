package org.dkf.jed2k.android;

import android.os.ParcelFileDescriptor;
import android.support.v4.provider.DocumentFile;
import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.FileHandler;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by inkpot on 31.01.2017.
 */
@Slf4j
public class AndroidFileHandler extends FileHandler {
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
