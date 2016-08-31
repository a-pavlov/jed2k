package org.dkf.jed2k.protocol;

import org.dkf.jed2k.exception.JED2KException;

import java.nio.ByteBuffer;

public abstract class SoftSerializable implements Serializable {
    public abstract ByteBuffer get(ByteBuffer src, int limit) throws JED2KException;
}
