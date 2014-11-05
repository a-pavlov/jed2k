package org.jed2k.protocol;

public class ClientSendingPart32 extends ClientSendingPart<UInt32> {
    @Override
    public int dataSize() {
        return endOffset.intValue() - beginOffset.intValue();
    }
}
