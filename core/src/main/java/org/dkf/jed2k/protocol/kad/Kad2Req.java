package org.dkf.jed2k.protocol.kad;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.dkf.jed2k.exception.JED2KException;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 21.11.2016.
 */
@Getter
@Setter
@ToString
public class Kad2Req extends Transaction {
    private byte searchType;
    private KadId target = new KadId();
    private KadId receiver = new KadId();

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        searchType = src.get();
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
}
