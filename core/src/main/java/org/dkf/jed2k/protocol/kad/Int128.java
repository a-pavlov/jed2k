package org.dkf.jed2k.protocol.kad;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.BitField;
import org.dkf.jed2k.protocol.Serializable;

import java.nio.ByteBuffer;


/**
 *
 * @author apavlov
 * 128 bit identifier in KAD network
 */
public class Int128 implements Serializable, Comparable<Int128> {

    private BitField data;

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int bytesCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int compareTo(Int128 arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

}
