package org.dkf.jed2k.protocol;

import org.dkf.jed2k.Utils;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import static org.dkf.jed2k.protocol.Unsigned.*;

/**
 * this is the same as Container class except it can store only bytes and array uses as container
 * @param <CS>
 */
public class ByteContainer<CS extends UNumber> implements Serializable {
    private static Logger log = LoggerFactory.getLogger(ByteBuffer.class.getName());

    public final CS size;
    public byte[] value;

    public ByteContainer(CS size) {
        this.size = size;
    }

    public ByteContainer(CS size, byte[] value) {
        this.size = size;
        this.value = value;
        this.size.assign(value.length);
    }

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        size.get(src);
        if (size.intValue() > 0) {
            value = new byte[size.intValue()];
            src.get(value);
        }

        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        if (value == null) {
            size.assign(0);
            return size.put(dst);
        } else {
            size.assign(value.length);
            return size.put(dst).put(value);
        }
    }

    public String asString() throws JED2KException {
        try {
            if (value != null)  return new String(value, "UTF-8");
            return "";
        } catch(UnsupportedEncodingException e) {
            throw new JED2KException(e, ErrorCode.UNSUPPORTED_ENCODING);
        }
    }

    public void assignString(final String value) throws JED2KException {
        try {
            this.value = value.getBytes("UTF-8");
            this.size.assign(this.value.length);
        } catch(UnsupportedEncodingException e) {
            throw new JED2KException(e, ErrorCode.UNSUPPORTED_ENCODING);
        }
    }

    public static<CS extends UNumber> ByteContainer<UInt8> fromString8(String value) throws JED2KException {
        try {
            byte[] content = value.getBytes("UTF-8");
            return new ByteContainer<UInt8>(uint8(), content);
        } catch(UnsupportedEncodingException e) {
            throw new JED2KException(e, ErrorCode.UNSUPPORTED_ENCODING);
        }
    }

    public static<CS extends UNumber> ByteContainer<UInt16> fromString16(String value) throws JED2KException {
        try {
            byte[] content = value.getBytes("UTF-8");
            return new ByteContainer<UInt16>(uint16(), content);
        } catch(UnsupportedEncodingException e) {
            throw new JED2KException(e, ErrorCode.UNSUPPORTED_ENCODING);
        }
    }

    public static<CS extends UNumber> ByteContainer<UInt32> fromString32(String value) throws JED2KException {
        try {
            byte[] content = value.getBytes("UTF-8");
            return new ByteContainer<UInt32>(uint32(), content);
        } catch(UnsupportedEncodingException e) {
            throw new JED2KException(e, ErrorCode.UNSUPPORTED_ENCODING);
        }
    }

    @Override
    public String toString() {
        return String.format("%d[%s]", size.intValue(), Utils.byte2String(value));
    }

    @Override
    public int bytesCount() {
        return size.bytesCount() + value.length;
    }

}