package org.dkf.jed2k.protocol.kad.test;

import org.dkf.jed2k.protocol.kad.KadId;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by inkpot on 14.11.2016.
 */
public class KadFunctionsTest {

    @Test
    public void testDistanceExp() {
        KadId n1 = KadId.fromString("514d5f30f05328a05b94c140aa412fd3");
        KadId n2 = KadId.fromString("514d5f30f05328a05b94c140aa412fd3");
        assertEquals(0, KadId.distanceExp(n1, n2));
        KadId n3 = KadId.fromString("D14d5f30f05328a05b94c140aa412fd3");
        assertEquals(127, KadId.distanceExp(n1, n3));
        assertEquals(124, KadId.distanceExp(n3, KadId.fromString("CA4d5f30f05328a05b94c140aa412fd3")));
        assertEquals(99, KadId.distanceExp(n2, KadId.fromString("514d5f38f05328a05b94c140aa412fd3")));
    }

    @Test
    public void testCompareRef() {
        KadId ref = KadId.fromString("514d5f30f05328a05b94c140aa412fd3");
        KadId n1 = KadId.fromString("514d5f30f05328a05b94c140aa412fd3");
        KadId n2 = KadId.fromString("524d5f30f05328a05b94c140aa412fd3");
        assertEquals(1, KadId.compareRef(n1, n2, ref));
        assertEquals(1, KadId.compareRef(n2, KadId.fromString("624d5f30f05328a05b94c140aa412fd3"), ref));
    }
}
