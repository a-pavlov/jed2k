package org.jed2k.protocol.client;

import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.Dispatchable;
import org.jed2k.protocol.Dispatcher;
import org.jed2k.protocol.UInt64;
import org.jed2k.protocol.Unsigned;

/**
 * Created by ap197_000 on 22.08.2016.
 */
public class CompressedPart64 extends CompressedPart<UInt64> implements Dispatchable {

    public CompressedPart64() {
        beginOffset = Unsigned.uint64(0);
    }

    @Override
    public void dispatch(Dispatcher dispatcher) throws JED2KException {
        dispatcher.onClientCompressedPart64(this);
    }
}
