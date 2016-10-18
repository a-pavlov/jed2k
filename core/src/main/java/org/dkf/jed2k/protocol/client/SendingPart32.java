package org.dkf.jed2k.protocol.client;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.hash.MD4;
import org.dkf.jed2k.protocol.Dispatchable;
import org.dkf.jed2k.protocol.Dispatcher;
import org.dkf.jed2k.protocol.UInt32;
import org.dkf.jed2k.protocol.Unsigned;

public class SendingPart32 extends SendingPart<UInt32> implements Dispatchable {

    // for packet combiner
    public static int SIZE = MD4.HASH_SIZE + UInt32.SIZE*2;

    public SendingPart32() {
        beginOffset = Unsigned.uint32();
        endOffset   = Unsigned.uint32();
    }

    @Override
    public int payloadSize() {
        return endOffset.intValue() - beginOffset.intValue();
    }

    @Override
    public void dispatch(Dispatcher dispatcher) throws JED2KException {
        dispatcher.onClientSendingPart32(this);
    }
}
