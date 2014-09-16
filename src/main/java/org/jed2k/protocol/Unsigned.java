package org.jed2k.protocol;

public final class Unsigned{  
  public static UInt8 uint8() { return new UInt8(0);}
  public static UInt8 uint8(byte value){ return new UInt8(value);}
  public static UInt8 uint8(short value){ return new UInt8(value); }
  public static UInt8 uint8(int value){ return new UInt8(value); }  
}