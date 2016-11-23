package org.dkf.jed2k.protocol.kad;

import org.dkf.jed2k.exception.JED2KException;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 15.11.2016.
 */
public class Kad2BootstrapReq extends Transaction {
    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return dst;
    }

    @Override
    public int bytesCount() {
        return 0;
    }

    @Override
    public byte getTransactionId() {
        return Transaction.BOOTSTRAP;
    }
}
