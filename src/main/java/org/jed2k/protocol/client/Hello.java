package org.jed2k.protocol.client;

import java.nio.ByteBuffer;

import org.jed2k.exception.JED2KException;
import org.jed2k.hash.MD4;
import org.jed2k.protocol.Dispatchable;
import org.jed2k.protocol.Dispatcher;

import static org.jed2k.Utils.sizeof;

public class Hello extends HelloAnswer implements Dispatchable {
    public byte hashLength = (byte)MD4.HASH_SIZE;
    
    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        hashLength = src.get();
        return super.get(src);
    }
    
    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return super.put(dst.put(hashLength));
    }
    
    @Override
    public int bytesCount() {
        return sizeof(hashLength) + super.bytesCount();
    }

    @Override
    public void dispatch(Dispatcher dispatcher) throws JED2KException {
        dispatcher.onClientHello(this);
    }
}
