package org.dkf.jed2k.test.kad;

import org.dkf.jed2k.kad.DhtTracker;
import org.dkf.jed2k.kad.NodeImpl;
import org.dkf.jed2k.kad.RoutingTable;
import org.dkf.jed2k.kad.traversal.algorithm.FindKeywords;
import org.dkf.jed2k.kad.traversal.algorithm.Traversal;
import org.dkf.jed2k.kad.traversal.observer.Observer;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.Hash;
import org.dkf.jed2k.protocol.Serializable;
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

    @Test
    public void testTraversalToString() {
        Traversal t = new Traversal(new NodeImpl(new DhtTracker(100, new KadId(Hash.LIBED2K), null), new KadId(Hash.LIBED2K), 40000, null), new KadId(Hash.EMULE)) {

            public Traversal add() {
                results.add(newObserver(new Endpoint(100, 4444), new KadId(Hash.LIBED2K), 30000, (byte)0x33));
                results.add(newObserver(new Endpoint(100, 4444), new KadId(Hash.LIBED2K), 30001, (byte)0x33));
                results.add(newObserver(new Endpoint(100, 4444), new KadId(Hash.LIBED2K), 30002, (byte)0x33));
                return this;
            }

            @Override
            public Observer newObserver(Endpoint endpoint, KadId id, int portTcp, byte version) {
                return new Observer(this, endpoint, id, portTcp, version) {
                    @Override
                    public void reply(Serializable s, Endpoint endpoint) {

                    }

                    @Override
                    public boolean isExpectedTransaction(Serializable s) {
                        return false;
                    }
                };
            }

            @Override
            public boolean invoke(Observer o) {
                return false;
            }
        }.add();

        String s = t.toString();
        assertFalse(s.isEmpty());
    }
}
