package org.dkf.jed2k.protocol;

import org.dkf.jed2k.exception.JED2KException;

public abstract class Buffer {
    public abstract Buffer get(UInt8 v) throws JED2KException;
    public abstract Buffer put(UInt8 v) throws JED2KException;
    public abstract Buffer get(UInt16 v) throws JED2KException;
    public abstract Buffer put(UInt16 v) throws JED2KException;
    public abstract Buffer get(UInt32 v) throws JED2KException;
    public abstract Buffer put(UInt32 v) throws JED2KException;
    public abstract Buffer get(UInt64 v) throws JED2KException;
    public abstract Buffer put(UInt64 v) throws JED2KException;
    public abstract Buffer get(byte[] v) throws JED2KException;
    public abstract Buffer put(byte[] v) throws JED2KException;

    public abstract Buffer put(byte v) throws JED2KException;
    public abstract Buffer put(short v) throws JED2KException;
    public abstract Buffer put(int v) throws JED2KException;
    public abstract Buffer put(float v) throws JED2KException;

    public abstract byte getByte() throws JED2KException;
    public abstract short getShort() throws JED2KException;
    public abstract int getInt() throws JED2KException;
    public abstract float getFloat() throws JED2KException;
    public abstract Buffer position(int newPosition) throws JED2KException;
    public abstract int limit();
    public abstract int remaining();
}