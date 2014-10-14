package org.jed2k.protocol;

import static org.jed2k.protocol.Unsigned.uint16;

public class ServerMessage extends ByteContainer<UInt16> {
    public ServerMessage() {
        super(uint16());
    }
}
