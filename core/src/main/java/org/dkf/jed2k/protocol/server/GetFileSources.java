package org.dkf.jed2k.protocol.server;

import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Hash;
import org.dkf.jed2k.protocol.Serializable;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import static org.dkf.jed2k.Utils.sizeof;

public class GetFileSources implements Serializable {
    public Hash hash;
    public int lowPart  = 0;
    public int hiPart   = 0;

    public GetFileSources() {
        hash = new Hash();
    }

    public GetFileSources(Hash h, int hi, int lo) {
        hash = h;
        lowPart = lo;
        hiPart = hi;
    }

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        try {
            hash.get(src);
            int val = src.getInt();

            if (val == 0) {
                // high part exists
                lowPart = src.getInt();
                hiPart  = src.getInt();
            } else {
                lowPart = val;
            }

            return src;
        } catch(BufferUnderflowException e) {
            throw new JED2KException(ErrorCode.BUFFER_UNDERFLOW_EXCEPTION);
        } catch(Exception e) {
            throw new JED2KException(ErrorCode.BUFFER_GET_EXCEPTION);
        }
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        hash.put(dst);

        // eDonkey protocol specific - mark we have hiPart in size here
        if (hiPart != 0) {
            dst.putInt(0);
        }

        // push low part
        dst.putInt(lowPart);

        // push hi part if exists
        if (hiPart != 0) {
            dst.putInt(hiPart);
        }

        return dst;
    }

    @Override
    public int bytesCount() {
        return hash.bytesCount() + sizeof(lowPart) + ((hiPart != 0)?sizeof(hiPart):0);
    }

}