package org.dkf.jed2k.protocol;

import org.dkf.jed2k.exception.JED2KException;

import java.nio.ByteBuffer;

public interface Serializable {
    public ByteBuffer get(ByteBuffer src) throws JED2KException;
    public ByteBuffer put(ByteBuffer dst) throws JED2KException;
    public int bytesCount();
}