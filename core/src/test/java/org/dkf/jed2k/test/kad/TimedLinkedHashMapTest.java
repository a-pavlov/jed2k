package org.dkf.jed2k.test.kad;

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

        public long getCreateTime() {
            return this.createTime;
        }

        public String getValue() {
            return this.value;
        }

        public void setCreateTime(long createTime) {
            this.createTime = createTime;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public boolean equals(final Object o) {
            if (o == this) return true;
            if (!(o instanceof TestItem)) return false;
            final TestItem other = (TestItem) o;
            if (!other.canEqual((Object) this)) return false;
            if (this.createTime != other.createTime) return false;
            final Object this$value = this.value;
            final Object other$value = other.value;
            if (this$value == null ? other$value != null : !this$value.equals(other$value)) return false;
            return true;
        }

        protected boolean canEqual(final Object other) {
            return other instanceof TestItem;
        }

        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final long $createTime = this.createTime;
            result = result * PRIME + (int) ($createTime >>> 32 ^ $createTime);
            final Object $value = this.value;
            result = result * PRIME + ($value == null ? 43 : $value.hashCode());
            return result;
        }

        public String toString() {
            return "TimedLinkedHashMapTest.TestItem(createTime=" + this.createTime + ", value=" + this.value + ")";
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
