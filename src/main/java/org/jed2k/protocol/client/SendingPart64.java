package org.jed2k.protocol.client;

import org.jed2k.hash.MD4;
import org.jed2k.protocol.UInt64;
import org.jed2k.protocol.Unsigned;

public class SendingPart64 extends SendingPart<UInt64> {
    
    // for packet combiner
    public static int SIZE = MD4.HASH_SIZE + UInt64.SIZE*2;
    
    public SendingPart64() {
        beginOffset = Unsigned.uint64();
        endOffset   = Unsigned.uint64();
    }
    
    @Override
    public int payloadSize() {
        return (int)(endOffset.longValue() - beginOffset.longValue());
    }
}
