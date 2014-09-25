package org.jed2k.protocol;

public interface Serializable{
    public Buffer get(Buffer src) throws ProtocolException;
    public Buffer put(Buffer dst) throws ProtocolException;
}