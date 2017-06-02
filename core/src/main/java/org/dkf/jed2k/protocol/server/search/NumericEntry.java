package org.dkf.jed2k.protocol.server.search;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.ByteContainer;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.UInt16;

import java.nio.ByteBuffer;

import static org.dkf.jed2k.Utils.sizeof;

public class NumericEntry implements Serializable {

    private static long UINT_MAX = 0xffffffffL;
    private long value;
    private byte operator;
    private ByteContainer<UInt16> tag;

    NumericEntry(long value, byte operator, ByteContainer<UInt16> tag) {
        this.value = value;
        this.operator = operator;
        this.tag = tag;
        assert(tag != null);
    }

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        assert(false);
        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        if (value <= UINT_MAX) {
            dst.put(SearchRequest.SEARCH_TYPE_UINT32);
        } else {
            dst.put(SearchRequest.SEARCH_TYPE_UINT64);
        }

        if (value <= UINT_MAX) {
            dst.putInt((int)value);
        } else {
            dst.putLong(value);
        }

        dst.put(operator);

        return tag.put(dst);
    }

    @Override
    public int bytesCount() {
        return sizeof(SearchRequest.SEARCH_TYPE_UINT32) + ((value <= UINT_MAX)?sizeof(value)/2:sizeof(value)) + sizeof(operator) + tag.bytesCount();
    }

    @Override
    public String toString() {
        return String.format("[%d]%d_t%s", operator, value, tag.toString());
    }
}
