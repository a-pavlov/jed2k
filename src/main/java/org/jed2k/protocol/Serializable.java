package org.jed2k.protocol;

public interface Serializable{
    public Buffer get(Buffer src);
    public Buffer put(Buffer dst);
}