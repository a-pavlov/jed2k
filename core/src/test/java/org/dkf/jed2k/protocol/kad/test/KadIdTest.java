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


    @Test
    public void testGetPut() throws JED2KException {
        KadId kids[] = {
                KadId.fromString("514d5f30f05328a05b94c140aa412fd3"),
                KadId.fromString("59c729f19e6bc2ab269d99917bceb5a0"),
                KadId.fromString("44d847c1c5e8d910d4200db8b464dbf4")
        };

        ByteBuffer bb = ByteBuffer.allocate(MD4.HASH_SIZE*kids.length);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        for(final KadId kid: kids) {
            kid.put(bb);
        }

        assertFalse(bb.hasRemaining());

        bb.flip();
        for(int i = 0; i < kids.length; ++i) {
            KadId kid = new KadId();
            kid.get(bb);
            assertEquals(kids[i], kid);
        }
    }
}
