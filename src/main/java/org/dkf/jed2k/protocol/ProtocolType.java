package org.dkf.jed2k.protocol;

import static org.dkf.jed2k.protocol.Unsigned.uint8;

enum ProtocolType {
  OP_EDONKEYHEADER(0xE3),
  OP_EDONKEYPROT(0xE3),
  OP_PACKEDPROT(0xD4),
  OP_EMULEPROT(0xC5);

  public UInt8 value;

  ProtocolType(int v) {
      this.value = uint8(v);
  }
}