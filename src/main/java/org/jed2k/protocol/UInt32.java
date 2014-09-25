package org.jed2k.protocol;


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
    
    
    private int value;

    
    public UInt32(){
      value = 0;
    }
    
    public UInt32(byte value){
      this.value = (int)value;
    }
    
    public UInt32(short value){
      this.value = (int)(value);
    }
    
    public UInt32(int value){
      this.value = value;
    }

    @Override
    public Buffer get(Buffer src) throws ProtocolException {
        return src.get(this);        
    }


    @Override
    public Buffer put(Buffer dst) throws ProtocolException {
        return dst.put(this);
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
}