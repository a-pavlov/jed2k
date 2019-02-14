package org.dkf.jed2k.protocol.kad;

import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.kad.ReqDispatcher;
import org.dkf.jed2k.protocol.Serializable;

import java.net.InetSocketAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * Created by inkpot on 21.11.2016.
 */
public class Kad2Req implements Serializable, KadDispatchable {
    private byte searchType;
    private KadId target = new KadId();
    private KadId receiver = new KadId();

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        try {
            searchType = src.get();
        } catch(BufferUnderflowException e) {
            throw new JED2KException(ErrorCode.BUFFER_UNDERFLOW_EXCEPTION);
        } catch(Exception e) {
            throw new JED2KException(ErrorCode.BUFFER_GET_EXCEPTION);
        }
        return receiver.get(target.get(src));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return receiver.put(target.put(dst.put(searchType)));
    }

    @Override
    public int bytesCount() {
        return 1 + target.bytesCount() + receiver.bytesCount();
    }

    @Override
    public void dispatch(ReqDispatcher dispatcher, final InetSocketAddress address) {
        dispatcher.process(this, address);
    }

    public byte getSearchType() {
        return this.searchType;
    }

    public KadId getTarget() {
        return this.target;
    }

    public KadId getReceiver() {
        return this.receiver;
    }

    public void setSearchType(byte searchType) {
        this.searchType = searchType;
    }

    public void setTarget(KadId target) {
        this.target = target;
    }

    public void setReceiver(KadId receiver) {
        this.receiver = receiver;
    }

    public String toString() {
        return "Kad2Req(searchType=" + this.getSearchType() + ", target=" + this.getTarget() + ", receiver=" + this.getReceiver() + ")";
    }
}
