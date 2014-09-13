package org.jed2k.protocol;

import org.jed2k.number.UByte;
import org.jed2k.number.UInteger;
import org.jed2k.number.ULong;
import org.jed2k.number.UShort;

public abstract class Buffer{

    public abstract Buffer put(UByte v);
    public abstract Buffer get(UByte v);
    public abstract Buffer put(UShort v);
    public abstract Buffer get(UShort v);
    public abstract Buffer put(UInteger v);
    public abstract Buffer get(UInteger v);
    public abstract Buffer put(ULong v);
    public abstract Buffer get(ULong v);
}