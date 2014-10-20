package org.jed2k.protocol;

import java.nio.ByteBuffer;
import org.jed2k.exception.JED2KException;

public abstract class SoftSerializable implements Serializable {
    public abstract ByteBuffer get(ByteBuffer src, int limit) throws JED2KException;
}
