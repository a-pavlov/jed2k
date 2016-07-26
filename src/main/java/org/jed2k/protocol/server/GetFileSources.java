package org.jed2k.protocol.server;

import java.nio.ByteBuffer;
import static org.jed2k.Utils.sizeof;
import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.Hash;
import org.jed2k.protocol.Serializable;

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
        hash.get(src);
        int val = src.getInt();

        if (val == 0) {
            // high part exists
            lowPart = src.getInt();
            hiPart  = src.getInt();
        } else {
            lowPart = val;
        }

        return null;
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