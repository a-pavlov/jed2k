package org.dkf.jed2k.protocol.kad.test;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.hash.MD4;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.Hash;
import org.dkf.jed2k.protocol.kad.KadId;
import org.junit.Test;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.*;

/**
 * Created by inkpot on 14.11.2016.
 */
public class KadIdTest {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(KadIdTest.class);

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

    @Test
    public void trivialTest() {
        byte a = 10;
        byte b = 2;
        assertEquals(8, a ^ b);
    }

    @Test
    public void testBitsOperations() {
        KadId id = new KadId();
        assertTrue(id.isAllZeros());
        for(int i = 0; i != id.TOTAL_BITS / 8; ++i) id.set(i, (byte)0x80);
        KadId template = new KadId();
        for(int i = 0; i != id.TOTAL_BITS / 8; ++i) template.set(i, (byte)0x7F);
        assertEquals(template, id.bitsInverse());
        assertEquals(new KadId(), template.bitsAnd(id));
        assertEquals(id, template.bitsOr(id));
    }

    @Test
    public void testIdGenerator() {
        KadId id = KadId.generateId(new Endpoint(new InetSocketAddress("81.168.99.44", 8999)).getIP(), 0);
        assertTrue(id != null);
    }

    @Test
    public void testKadIdForBucketGeneration() {
        final KadId target = new KadId(KadId.fromString("F8A8AFE3018B38D9B4D880D0683CCEB5"));
        log.debug("{}", target);
        for(int i = 0; i < KadId.TOTAL_BITS; ++i) {
            KadId id = KadId.generateRandomWithinBucket(i, target);
            assertEquals(KadId.TOTAL_BITS - 1 - i, KadId.distanceExp(target, id));
            log.debug("{} <-- {}", id, i);
        }
    }

    @Test
    public void testWithHashUsage() {
        Set<Hash> data = new HashSet<>();
        data.add(Hash.EMULE);
        data.add(Hash.TERMINAL);
        data.add(Hash.LIBED2K);
        assertTrue(data.contains(new KadId(Hash.EMULE)));
        assertTrue(data.contains(new KadId(Hash.TERMINAL)));
        assertTrue(data.contains(new KadId(Hash.LIBED2K)));
        assertFalse(data.contains(new KadId(Hash.random(false))));
    }

    @Test
    public void testEquals() {
        KadId id = new KadId(Hash.EMULE);
        assertEquals(id, id);
        assertEquals(new KadId(), new KadId());
        assertFalse(id.equals(new KadId()));
        assertEquals((new KadId(Hash.fromString("963663108FE8541EF2CD0280167F2021"))), new KadId(Hash.fromString("963663108FE8541EF2CD0280167F2021")));
        assertFalse(new KadId(Hash.fromString("963663108FE8541EF2CD0280167F2021")).equals(new KadId(Hash.fromString("963663108FE8541EF2CD0280167F2020"))));
    }
}
