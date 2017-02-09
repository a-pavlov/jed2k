package org.dkf.jed2k.protocol;

import org.dkf.jed2k.exception.JED2KException;

import java.nio.ByteBuffer;

/**
 * will be used to save search entry
 * Created by inkpot on 05.01.2017.
 */
public class SearchEntryItem implements Serializable {

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return null;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return null;
    }

    @Override
    public int bytesCount() {
        return 0;
    }
}
