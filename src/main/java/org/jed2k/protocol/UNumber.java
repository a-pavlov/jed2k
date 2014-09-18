package org.jed2k.protocol;

public abstract class UNumber extends Number implements Serializable {
  private static final long serialVersionUID = 199898L;  
  
  
  public abstract UNumber assign(byte value);
  public abstract UNumber assign(short value);
  public abstract UNumber assign(int value);   
}