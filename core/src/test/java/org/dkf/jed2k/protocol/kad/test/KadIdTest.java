package org.dkf.jed2k.protocol.kad.test;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.hash.MD4;
import org.dkf.jed2k.protocol.kad.KadId;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

/**
 * Created by inkpot on 14.11.2016.
 */
public class KadIdTest {

    @Test
    public void testKadIdGet() throws JED2KException {
        int template[] = {0x00000102, 0x0304, 0x0506, 0x0708};
        byte result[] = {0x00, 0x00, 0x01, 0x02,
                0x00, 0x00, 0x03, 0x04,
                0x00, 0x00, 0x05, 0x06,
                0x00, 0x00, 0x07, 0x08
        };

        KadId kid = KadId.fromBytes(result);
        ByteBuffer bb = ByteBuffer.allocate(MD4.HASH_SIZE);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        kid.put(bb);
        assertFalse(bb.hasRemaining());
        bb.flip();
        for (int i = 0; i < template.length; ++i) {
            assertEquals(template[i], bb.getInt());
        }
    }
}
