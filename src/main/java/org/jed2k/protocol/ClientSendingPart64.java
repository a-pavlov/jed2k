package org.jed2k.protocol;

public class ClientSendingPart64 extends ClientSendingPart<UInt64> {
    @Override
    public int dataSize() {
        return (int)(endOffset.longValue() - beginOffset.longValue());
    }
}
