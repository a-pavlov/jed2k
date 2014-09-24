package org.jed2k.protocol;

public class UInt8 extends UNumber implements Comparable<UInt8> {
  
  private static final long    serialVersionUID = -6821055240959745390L;
  public static final short    MIN_VALUE        = 0x00;
  public static final short    MAX_VALUE        = 0xff;
  private byte container;
  
  UInt8(){
      container = 0;
  }
  
  UInt8(byte value){
    container = value;
  }
  
  UInt8(int value){
    container = (byte)value;
  }
  
  UInt8(short value){
    container = (byte)value;
  }
  
  @Override
  public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj instanceof UInt8) {
          return container == ((UInt8) obj).container;
      }

      return false;
  }
  
  @Override
  public int compareTo(UInt8 o) {    
    return (shortValue() < o.shortValue() ? -1 : (shortValue() == o.shortValue() ? 0 : 1));
  }
  
  @Override
  public double doubleValue() {
    return container & 0xff;    
  }
  
  @Override
  public float floatValue() {
    return container & MAX_VALUE;
  }
  
  @Override
  public int intValue() {
    return container & MAX_VALUE;
  }
  
  @Override
  public long longValue() {
    return container & MAX_VALUE;
  }  

  @Override
  public Buffer get(Buffer src) {
    return src.get(this);
  }

  @Override
  public Buffer put(Buffer dst) {
    return dst.put(this);
  }
  
  @Override
  public UNumber assign(byte b){
    container = b;
    return this;
  }

  @Override
  public UNumber assign(short value) {
    container = (byte)value;
    return this;
  }

  @Override
  public UNumber assign(int value) {
    container = (byte)value;
    return this;
  } 
}