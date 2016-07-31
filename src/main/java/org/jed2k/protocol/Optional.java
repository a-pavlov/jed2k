package org.jed2k.protocol;

import org.jed2k.exception.ErrorCode;
import org.jed2k.exception.JED2KException;

import java.nio.ByteBuffer;

/**
 * class for optional store some serializable structures
 * Created by inkpot on 31.07.2016.
 */
public class Optional<Data extends Serializable> implements Serializable {
    private Data data = null;
    private final Class<Data> clazz;

    public Optional(Class<Data> clazz) {
        this.clazz = clazz;
    }

    public void setData(Data d) {
        data = d;
        d.getClass();
    }

    public final boolean haveData() {
        return data != null;
    }

    public final Data getData() {
        return data;
    }

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        byte flag = src.get();
        if (flag == (byte)1) {
            try {
                data = clazz.newInstance();
                data.get(src);
            } catch (InstantiationException e) {
                throw new JED2KException(ErrorCode.GENERIC_INSTANTIATION_ERROR);
            } catch (IllegalAccessException e1) {
                throw new JED2KException(ErrorCode.GENERIC_ILLEGAL_ACCESS);
            }
        } else {
            data = null;
        }

        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        byte flag = (data != null)?(byte)1:(byte)0;
        dst.put(flag);
        if (data != null) data.put(dst);
        return dst;
    }

    @Override
    public int bytesCount() {
        return 1 + ((data != null)?data.bytesCount():0);
    }
}
