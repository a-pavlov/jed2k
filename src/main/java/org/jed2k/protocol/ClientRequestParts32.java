package org.jed2k.protocol;

public class ClientRequestParts32 extends ClientRequestParts<UInt32> {

    @Override
    public int bytesCount() {
        return hash.bytesCount() + PARTS_COUNT*2*UInt32.SIZE;
    }   
}
