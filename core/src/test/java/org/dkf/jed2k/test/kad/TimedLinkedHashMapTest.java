package org.dkf.jed2k.test.kad;

import lombok.Data;
import org.dkf.jed2k.Time;
import org.dkf.jed2k.kad.Timed;
import org.dkf.jed2k.kad.traversal.TimedLinkedHashMap;
import org.dkf.jed2k.protocol.Hash;
import org.dkf.jed2k.protocol.kad.KadId;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Created by inkpot on 24.01.2017.
 */
public class TimedLinkedHashMapTest {

    @Data
    private static class TestItem implements Timed {
        private long createTime;
        private String value;

        public TestItem(long createTime, final String value) {
            this.createTime = createTime;
            this.value = value;
        }

        @Override
        public long getLastActiveTime() {
            return createTime;
        }
    }

    @Before
    public void setUpTime() {
        Time.updateCachedTime();
    }

    @Test
    public void testFeatures() throws InterruptedException {
        TimedLinkedHashMap<KadId, TestItem> order = new TimedLinkedHashMap<>(10, 10, Time.seconds(1), 0);   // turn off removing by size
        order.put(new KadId(Hash.EMULE), new TestItem(Time.currentTime(), "emule"));
        order.put(new KadId(Hash.LIBED2K), new TestItem(Time.currentTime() + 22, "libed2k"));
        order.put(new KadId(Hash.INVALID), new TestItem(Time.currentTime() + 44, "invalid"));
        order.put(new KadId(Hash.TERMINAL), new TestItem(Time.currentTime() + Time.seconds(1), "terminal"));
        assertEquals(4, order.values().size());
        Thread.sleep(1050);
        Time.updateCachedTime();
        // remove eldest here
        order.put(new KadId(Hash.random(false)), new TestItem(Time.currentTime() + Time.seconds(1) + 20, "random 1"));
        assertEquals(4, order.values().size());
        assertFalse(order.containsKey(new KadId(Hash.EMULE)));
        assertTrue(order.containsKey(new KadId(Hash.LIBED2K)));

        Thread.sleep(60);
        Time.updateCachedTime();
        // access to libed2k, put new element and remove invalid
        TestItem item = order.get(new KadId(Hash.LIBED2K));
        item.setCreateTime(Time.currentTime());
        order.put(new KadId(Hash.random(false)), new TestItem(Time.currentTime() + Time.seconds(1) + 60, "random 2"));
        assertEquals(4, order.values().size());
        assertTrue(order.containsKey(new KadId(Hash.LIBED2K)));
        assertFalse(order.containsKey(new KadId(Hash.INVALID)));

        String template[] = {"terminal", "random 1", "libed2k", "random 2"};
        int i = 0;
        for(final TestItem ti: order.values()) {
            assertEquals(template[i++], ti.getValue());
        }
    }

}
