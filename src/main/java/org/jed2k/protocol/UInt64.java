package org.jed2k.protocol;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import org.jed2k.exception.JED2KException;

public class UInt64 extends UNumber implements Comparable<UInt64>{
    /**
     * Generated UID
     */
    private static final long            serialVersionUID      = -682105524095390L;

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
    
    public static final int SIZE = 8;
    
    
    private long value;

    
    public UInt64(){
      value = 0;
    }
    
    public UInt64(byte value){
      this.value = (int)value;
    }
    
    public UInt64(short value){
      this.value = (int)(value);
    }
    
    public UInt64(int value){
      this.value = value;
    }

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        value = src.getLong();
        return src;
    }


    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return dst.putLong(value);
    }


    @Override
    public int compareTo(UInt64 o) {        
        return (BigInteger.valueOf(value).compareTo(BigInteger.valueOf(o.value)));        
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
        return (int)value;        
    }

    @Override
    public long longValue() {
        return value;        
    }    

    @Override
    public UInt64 assign(int value){
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
    
    public UNumber assign(long value) {
        this.value = value;
        return this;
    }

    @Override
    public int bytesCount() {
        return 8;        
    }
    
    @Override
    public String toString() {
        return "uint64{" + longValue() + "}";
    }
}