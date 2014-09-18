package org.jed2k.protocol;

public final class Unsigned{  
  public static UInt8 uint8() { return new UInt8();}
  public static UInt8 uint8(byte value){ return new UInt8(value);}
  public static UInt8 uint8(short value){ return new UInt8(value); }
  public static UInt8 uint8(int value){ return new UInt8(value); }  
  
  public static UInt16 uint16() { return new UInt16(); }
  public static UInt16 uint16(byte value) { return new UInt16(value); }
  public static UInt16 uint16(short value) { return new UInt16(value); }
  public static UInt16 uint16(int value) { return new UInt16(value); }
  
  public static UInt32 uint32() { return new UInt32(); }
  public static UInt32 uint32(byte value) { return new UInt32(value); }
  public static UInt32 uint32(short value) { return new UInt32(value); }
  public static UInt32 uint32(int value) { return new UInt32(value); }
  
}