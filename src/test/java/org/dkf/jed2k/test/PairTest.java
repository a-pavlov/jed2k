package org.dkf.jed2k.test;

import org.dkf.jed2k.Pair;
import org.dkf.jed2k.protocol.client.Hello;
import org.junit.Test;

import static junit.framework.Assert.*;

public class PairTest {
    private Pair<Long, Long> longPair = Pair.make(10l, 11l);
    private Pair<Hello, Long> partialPair = Pair.make(new Hello(), 22l);
    private Pair<Hello, Hello> uncomparablePair = Pair.make(new Hello(), new Hello());

    @Test
    public void testPairEquals() {
        assertTrue(longPair.equals(Pair.make(10l, 11l)));
        assertFalse(longPair.equals(Pair.make(9l, 9l)));
        assertFalse(longPair.equals(Pair.make(10, 11)));
        assertFalse(longPair.equals(Pair.make("xxxx", 9l)));
    }

    @Test
    public void testPairComparators() {
        // full compare
        assertEquals(0, longPair.compareTo(Pair.make(10l, 11l)));
        assertEquals(1, longPair.compareTo(Pair.make(9l, 11l)));
        assertEquals(1, longPair.compareTo(Pair.make(10l, 10l)));
        assertEquals(1, longPair.compareTo(Pair.make(9l, 12l)));
        assertEquals(-1, longPair.compareTo(Pair.make(12l, 12l)));
        assertEquals(-1, longPair.compareTo(Pair.make(10l, 12l)));
        // partial compare
        assertEquals(0, partialPair.compareTo(Pair.make(new Hello(), 22l)));
        assertEquals(-1, partialPair.compareTo(Pair.make(new Hello(), 23l)));
        assertEquals(1, partialPair.compareTo(Pair.make(new Hello(), 21l)));
    }

    @Test(expected = java.lang.AssertionError.class)
    public void testAssert() {
        uncomparablePair.compareTo(uncomparablePair);
    }
}
