package org.jed2k.protocol;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class NetworkBuffer extends Buffer{
    private ByteBuffer originator;
    public NetworkBuffer(ByteBuffer originator){
        this.originator = originator;
        this.originator.order(ByteOrder.LITTLE_ENDIAN);
    }

    @Override
    public Buffer get(UInt8 v) {
      byte b[] = { 0 };
      originator.get(b, 0, 1);
      v.assign(b[0]);
      return this;
    }

    @Override
    public Buffer put(UInt8 v) {
      originator.put(new byte[] { v.byteValue() }, 0, 1);
      return this;
    }

    @Override
    public Buffer get(UInt16 v) {
        v.assign(originator.getShort());
        return this;
    }

    @Override
    public Buffer put(UInt16 v) {
        originator.putShort(v.shortValue());
        return this;
    }

    @Override
    public Buffer get(UInt32 v) {
        v.assign(originator.getInt());
        return this;
    }

    @Override
    public Buffer put(UInt32 v) {
        originator.putInt(v.intValue());
        return this;
    }

    @Override
    public Buffer get(byte[] v) {        
        originator.get(v);
        return this;
    }

    @Override
    public Buffer put(byte[] v) {
        originator.put(v);
        return this;
    }

    @Override
    public Buffer put(byte v) {
        originator.put(v);
        return this;
    }

    @Override
    public Buffer put(short v) {
        originator.putShort(v);
        return this;
    }

    @Override
    public Buffer put(int v) {
        originator.putInt(v);
        return this;
    }
    
    @Override
    public Buffer put(float v) {
        originator.putFloat(v);
        return this;
    }

    @Override
    public byte getByte() {
        return originator.get();
    }

    @Override
    public short getShort() {
        return originator.getShort();
    }

    @Override
    public int getInt() {
        return originator.getInt();
    }
    
    @Override
    public float getFloat() {
        return originator.getFloat();
    }
}