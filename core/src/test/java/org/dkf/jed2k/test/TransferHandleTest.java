package org.dkf.jed2k.test;

import org.dkf.jed2k.Session;
import org.dkf.jed2k.Transfer;
import org.dkf.jed2k.TransferHandle;
import org.dkf.jed2k.protocol.Hash;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

/**
 * Created by inkpot on 11.09.2016.
 */
public class TransferHandleTest {
    private Session session;
    private Transfer transfer;

    @Before
    public void setUp() {
        session = Mockito.mock(Session.class);
        transfer = Mockito.mock(Transfer.class);
        when(transfer.hash()).thenReturn(Hash.EMULE);
        when(transfer.size()).thenReturn(1000l);
    }

    @Test
    public void testGetters() {
        TransferHandle h1 = new TransferHandle(session, transfer);
        TransferHandle h2 = new TransferHandle(session);
        assertFalse(h1.equals(h2));
        assertEquals(1000l, h1.getSize());
        assertEquals(0, h2.getSize());
    }
}
