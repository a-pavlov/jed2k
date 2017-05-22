package org.dkf.jed2k.protocol;

import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;

import java.nio.BufferUnderflowException;
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
        if (d != null) {
            data = d;
            d.getClass();
        }
    }

    public final boolean haveData() {
        return data != null;
    }

    public final Data getData() {
        return data;
    }

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        byte flag;

        // TODO - use better code here
        try {
            flag = src.get();
        } catch(BufferUnderflowException e) {
            throw new JED2KException(ErrorCode.BUFFER_UNDERFLOW_EXCEPTION);
        } catch(Exception e) {
            throw new JED2KException(ErrorCode.BUFFER_GET_EXCEPTION);
        }

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

    /*@Override
    public boolean equals(Object o) {
        if (o instanceof Optional) {
            final Optional<?> opt = (Optional<?>)o;
            if (haveData() && opt.haveData()) {
                return getData().equals(opt.getData());
            }

            if (!haveData() && !opt.haveData()) return true;
        }

        return false;
    }*/

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Optional<?> optional = (Optional<?>) o;

        if (data != null ? !data.equals(optional.data) : optional.data != null) return false;
        return !(clazz != null ? !clazz.equals(optional.clazz) : optional.clazz != null);

    }

    @Override
    public int hashCode() {
        int result = data != null ? data.hashCode() : 0;
        result = 31 * result + (clazz != null ? clazz.hashCode() : 0);
        return result;
    }
}
