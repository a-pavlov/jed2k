package org.jed2k.protocol.client;

import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.Dispatchable;
import org.jed2k.protocol.Dispatcher;
import org.jed2k.protocol.UInt32;
import org.jed2k.protocol.Unsigned;

/**
 * Created by ap197_000 on 22.08.2016.
 */
public class CompressedPart32 extends CompressedPart<UInt32> implements Dispatchable {

    public CompressedPart32() {
        beginOffset = Unsigned.uint32(0);
    }

    @Override
    public void dispatch(Dispatcher dispatcher) throws JED2KException {
        dispatcher.onClientCompressedPart32(this);
    }
}
