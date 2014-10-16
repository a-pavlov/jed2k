package org.jed2k.protocol;

import java.nio.ByteBuffer;

public abstract class SoftSerializable implements Serializable {
    public abstract ByteBuffer get(ByteBuffer src, int limit);
}
