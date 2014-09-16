package org.jed2k.protocol;

import static org.jed2k.protocol.Unsigned.uint8;

public class PacketHeader implements Serializable {
  public UInt8 protocol = uint8();
  //public UInt32 size = new Int32();
  public UInt8 packet = uint8();
  private boolean defined = false;

  public final boolean isDefined() {
      return defined;
  }

  public void setUndefined() {
      defined = false;
  }

  @Override
  public String toString() {
      return new String("Protocol: " + protocol + " size: " + " packet: " + packet);
  }

  @Override
  public Buffer get(Buffer src) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Buffer put(Buffer dst) {
    // TODO Auto-generated method stub
    return null;
  }
};