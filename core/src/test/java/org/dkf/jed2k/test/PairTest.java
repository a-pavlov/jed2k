package org.dkf.jed2k.test;

import org.dkf.jed2k.Pair;
import org.dkf.jed2k.protocol.client.Hello;
import org.junit.Assume;
import org.junit.Test;

import static junit.framework.Assert.*;

public class PairTest {
    private Pair<Long, Long> longPair = Pair.make(10L, 11L);
    private Pair<Hello, Long> partialPair = Pair.make(new Hello(), 22L);
    private Pair<Hello, Hello> uncomparablePair = Pair.make(new Hello(), new Hello());

    @Test
    public void testPairEquals() {
        assertTrue(longPair.equals(Pair.make(10L, 11L)));
        assertFalse(longPair.equals(Pair.make(9L, 9L)));
        assertFalse(longPair.equals(Pair.make(10, 11)));
        assertFalse(longPair.equals(Pair.make("xxxx", 9L)));
    }

    @Test
    public void testPairComparators() {
        // full compare
        assertEquals(0, longPair.compareTo(Pair.make(10L, 11L)));
        assertEquals(1, longPair.compareTo(Pair.make(9L, 11L)));
        assertEquals(1, longPair.compareTo(Pair.make(10L, 10L)));
        assertEquals(1, longPair.compareTo(Pair.make(9L, 12L)));
        assertEquals(-1, longPair.compareTo(Pair.make(12L, 12L)));
        assertEquals(-1, longPair.compareTo(Pair.make(10L, 12L)));
        // partial compare
        assertEquals(0, partialPair.compareTo(Pair.make(new Hello(), 22L)));
        assertEquals(-1, partialPair.compareTo(Pair.make(new Hello(), 23L)));
        assertEquals(1, partialPair.compareTo(Pair.make(new Hello(), 21L)));
    }

    @Test(expected = java.lang.AssertionError.class)
    public void testAssert() {
        Assume.assumeTrue(!System.getProperty("java.runtime.name").toLowerCase().startsWith("android"));
        uncomparablePair.compareTo(uncomparablePair);
    }
}
