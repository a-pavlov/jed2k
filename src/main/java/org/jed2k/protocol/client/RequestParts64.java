package org.jed2k.protocol.client;

import org.jed2k.protocol.UInt64;

public class RequestParts64 extends RequestParts<UInt64> {

    @Override
    public int bytesCount() {
        return hash.bytesCount() + PARTS_COUNT*2*UInt64.SIZE;
    }   
}
