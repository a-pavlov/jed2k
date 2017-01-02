package org.dkf.jed2k.test.kad;

import org.dkf.jed2k.kad.NodeImpl;
import org.dkf.jed2k.kad.RoutingTable;
import org.dkf.jed2k.kad.traversal.algorithm.FindKeywords;
import org.dkf.jed2k.protocol.Hash;
import org.dkf.jed2k.protocol.kad.KadId;
import org.junit.Assume;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

/**
 * Created by inkpot on 27.12.2016.
 */
public class TraversalTest {

    @Test
    public void testEquals() {
        Assume.assumeTrue(!System.getProperty("java.runtime.name").toLowerCase().startsWith("android"));
        NodeImpl node = Mockito.mock(NodeImpl.class);
        when(node.getTable()).thenReturn(new RoutingTable(new KadId(), 10));
        assertFalse(new FindKeywords(node, new KadId(Hash.LIBED2K), null).equals(new FindKeywords(node, new KadId(Hash.EMULE), null)));
    }
}
