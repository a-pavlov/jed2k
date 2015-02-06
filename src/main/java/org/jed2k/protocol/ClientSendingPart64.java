package org.jed2k.protocol;

import org.jed2k.hash.MD4;

public class ClientSendingPart64 extends ClientSendingPart<UInt64> {
    
    // for packet combiner
    public static int SIZE = MD4.HASH_SIZE + UInt64.SIZE*2;
    
    public ClientSendingPart64() {
        beginOffset = Unsigned.uint64();
        endOffset   = Unsigned.uint64();
    }
    
    @Override
    public int payloadSize() {
        return (int)(endOffset.longValue() - beginOffset.longValue());
    }
}
