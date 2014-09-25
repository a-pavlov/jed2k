package org.jed2k.protocol;

public abstract class Buffer {    
    public abstract Buffer get(UInt8 v) throws ProtocolException;
    public abstract Buffer put(UInt8 v) throws ProtocolException;
    public abstract Buffer get(UInt16 v) throws ProtocolException;
    public abstract Buffer put(UInt16 v) throws ProtocolException;
    public abstract Buffer get(UInt32 v) throws ProtocolException;
    public abstract Buffer put(UInt32 v) throws ProtocolException;
    public abstract Buffer get(byte[] v) throws ProtocolException;
    public abstract Buffer put(byte[] v) throws ProtocolException;
    
    public abstract Buffer put(byte v) throws ProtocolException;
    public abstract Buffer put(short v) throws ProtocolException;
    public abstract Buffer put(int v) throws ProtocolException;
    public abstract Buffer put(float v) throws ProtocolException;
    
    public abstract byte getByte() throws ProtocolException;
    public abstract short getShort() throws ProtocolException;
    public abstract int getInt() throws ProtocolException;
    public abstract float getFloat() throws ProtocolException;
}