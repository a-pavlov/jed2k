package org.jed2k.protocol.client;

import org.jed2k.protocol.UInt32;

public class RequestParts32 extends RequestParts<UInt32> {

    @Override
    public int bytesCount() {
        return hash.bytesCount() + PARTS_COUNT*2*UInt32.SIZE;
    }   
}
