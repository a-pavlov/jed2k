package org.jed2k.protocol;

import java.nio.ByteBuffer;
import org.jed2k.exception.JED2KException;

public interface Serializable {
    public ByteBuffer get(ByteBuffer src) throws JED2KException;
    public ByteBuffer put(ByteBuffer dst) throws JED2KException;
    public int size();    
}