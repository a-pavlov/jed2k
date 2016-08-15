package org.jed2k.protocol.client;

import org.jed2k.Constants;
import org.jed2k.protocol.Hash;
import org.jed2k.protocol.UInt32;
import org.jed2k.protocol.Unsigned;

public class RequestParts32 extends RequestParts<UInt32> {

    public RequestParts32() {
        super();
    }

    public RequestParts32(Hash h) {
        super(h);
    }

    @Override
    void initializeRanges() {
        for(int i = 0; i < beginOffset.length; ++i) beginOffset[i] = Unsigned.uint32(0);
        for(int i = 0; i < endOffset.length; ++i) endOffset[i] = Unsigned.uint32(0);
    }

    @Override
    public int bytesCount() {
        return hash.bytesCount() + Constants.PARTS_IN_REQUEST*2*UInt32.SIZE;
    }

    @Override
    public String toString() {
        return "RequestParts32 " + super.toString();
    }
}
