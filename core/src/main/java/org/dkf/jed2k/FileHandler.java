package org.dkf.jed2k;

import org.dkf.jed2k.exception.JED2KException;

import java.io.File;
import java.nio.channels.FileChannel;

/**
 * Created by inkpot on 30.01.2017.
 * common file interface with open and close(delete) feature
 */
public interface FileHandler {
    FileChannel getWriteChannel() throws JED2KException;
    FileChannel getReadChannel() throws JED2KException;
    void close() throws JED2KException;
    void delete() throws JED2KException;
    File getFile();
}
