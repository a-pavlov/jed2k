package org.dkf.jed2k.test;

import org.dkf.jed2k.Session;
import org.dkf.jed2k.SessionTrial;
import org.dkf.jed2k.Settings;
import org.dkf.jed2k.TransferHandle;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.Hash;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by inkpot on 24.08.2016.
 */
public class SessionTest {
    private Session session;
    private Settings settings;

    @Before
    public void setupSession() {
        settings = new Settings();
        settings.listenPort = -1;
        session = new SessionTrial(settings, new LinkedList<Endpoint>());
    }

    @Test
    public void testTransferHandle() throws JED2KException, InterruptedException {
        session.start();
        TransferHandle handle = session.addTransfer(Hash.EMULE, 1000L, "xxx");
        assertTrue(handle.isValid());
        TransferHandle handle2 = session.addTransfer(Hash.EMULE, 1002L, "yyy");
        assertEquals(1, session.getTransfers().size());
        assertEquals(handle, handle2);
        assertEquals(1000L, handle2.getSize());
        assertEquals("xxx", handle2.getFilePath().getName());
        assertTrue(handle2.getPeersInfo().isEmpty());
        session.removeTransfer(handle.getHash(), true);
        session.abort();
        session.join();
        assertEquals(0, session.getTransfers().size());
    }
}
