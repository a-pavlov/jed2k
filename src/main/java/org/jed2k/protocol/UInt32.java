package org.jed2k.protocol;

import static org.jed2k.Utils.sizeof;
import org.jed2k.exception.JED2KException;
import java.nio.ByteBuffer;

public class UInt32 extends UNumber implements Comparable<UInt32>{
    /**
     * Generated UID
     */
    private static final long            serialVersionUID      = -6821055240959745390L;

    /**
     * A constant holding the minimum value an <code>unsigned int</code> can
     * have, 0.
     */
    public static final long             MIN_VALUE             = 0x00000000;

    /**
     * A constant holding the maximum value an <code>unsigned int</code> can
     * have, 2<sup>32</sup>-1.
     */
    public static final long             MAX_VALUE             = 0xffffffffL;

    public static final int SIZE = 4;


    private int value;


    public UInt32(){
      value = 0;
    }

    public UInt32(byte value) {
      this.value = (int)value;
    }

    public UInt32(short value) {
      this.value = (int)(value) & 0xffff;
    }

    public UInt32(int value) {
      this.value = value;
    }

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        value = src.getInt();
        return src;
    }


    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return dst.putInt(value);
    }


    @Override
    public int compareTo(UInt32 o) {
        if (longValue() < o.longValue()) return -1;
        if (longValue() > o.longValue()) return 1;
        return 0;
    }

    @Override
    public double doubleValue() {
        return value;
    }


    @Override
    public float floatValue() {
        return value;
    }


    @Override
    public int intValue() {
        return value;
    }

    @Override
    public long longValue() {
        return value & MAX_VALUE;
    }

    @Override
    public UNumber assign(long value) {
        this.value = (int)(value);
        return this;
    }

    @Override
    public UInt32 assign(int value){
        this.value = value;
        return this;
    }

    @Override
    public UNumber assign(byte value) {
      this.value = value;
      return this;
    }

    @Override
    public UNumber assign(short value) {
      this.value = value;
      return this;
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj instanceof UInt32) {
            return value == ((UInt32) obj).value;
        }

        return false;
    }

    @Override
    public int bytesCount() {
        return sizeof(value);
    }

    @Override
    public String toString() {
        return "uint32{" + intValue() + "}";
    }

}