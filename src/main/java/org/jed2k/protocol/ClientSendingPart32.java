package org.jed2k.protocol;

import org.jed2k.hash.MD4;

public class ClientSendingPart32 extends ClientSendingPart<UInt32> {
    
    // for packet combiner
    public static int SIZE = MD4.HASH_SIZE + UInt32.SIZE*2;
    
    public ClientSendingPart32() {
        beginOffset = Unsigned.uint32();
        endOffset   = Unsigned.uint32();
    }
    
    @Override
    public int payloadSize() {
        return endOffset.intValue() - beginOffset.intValue();
    }
}
