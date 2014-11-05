package org.jed2k.protocol;

public class ClientRequestParts64 extends ClientRequestParts<UInt64> {

    @Override
    public int bytesCount() {
        return hash.bytesCount() + PARTS_COUNT*2*UInt64.SIZE;
    }   
}
