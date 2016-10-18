package org.dkf.jed2k.protocol.client;

import org.dkf.jed2k.Constants;
import org.dkf.jed2k.protocol.Hash;
import org.dkf.jed2k.protocol.UInt64;
import org.dkf.jed2k.protocol.Unsigned;

public class RequestParts64 extends RequestParts<UInt64> {

    public RequestParts64() {
        super();
    }

    public RequestParts64(Hash h) {
        super(h);
    }

    @Override
    public int bytesCount() {
        return hash.bytesCount() + Constants.PARTS_IN_REQUEST*2*UInt64.SIZE;
    }

    @Override
    void initializeRanges() {
        for(int i = 0; i < beginOffset.length; ++i) beginOffset[i] = Unsigned.uint64(0);
        for(int i = 0; i < endOffset.length; ++i) endOffset[i] = Unsigned.uint64(0);
    }

    @Override
    public String toString() {
        return "RequestParts64 " + super.toString();
    }
}
